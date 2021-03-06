package demo.cosmos.model;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;


/**
 * PsCMSContentRecordImpl
 *
 * @author johnjiang
 * @version V1.0
 * @ClassName PsCMSContentRecordImpl
 * @Description PsCMSContentRecordImpl
 * @date Apr 23, 2018
 */
@Component
public class PsCMSContentRecordImpl implements PsCMSContentRecord {

    private final static String TABLE_NAME = "t_content_record";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(ContentRecord contentRecord) {
        mongoTemplate.save(contentRecord);
    }
    public void saveSum(ContentRecordSummer contentRecordSummer) {
        mongoTemplate.save(contentRecordSummer);
    }

    @Override
    public List<ContentRecordSummer> findByContentIdAndUserIdInAndStartTimeAndEndTime(String contentId,
                                                                                      List<String> userIdList, long startTime, long endTime) {
        List<ContentRecordSummer> contentRecordSummer = new ArrayList<>();

        if (userIdList != null && userIdList.size() > 1000) {
            int num;
            int size = 1000;
            int total = userIdList.size();
            num = total / size;
            List<String> list = null;
            for (int i = 0; i < num; i++) {
                list = userIdList.subList(i * size, (i + 1) * size);
                List<ContentRecordSummer> summer = getContentRecordSummer(contentId, list, startTime, endTime);
                if (summer != null && summer.size() > 0) {
                    contentRecordSummer.addAll(summer);
                }

            }

        } else {
            contentRecordSummer = getContentRecordSummer(contentId, userIdList, startTime, endTime);
        }

        return contentRecordSummer;
    }

    private List<ContentRecordSummer> getContentRecordSummer(String contentId, List<String> userIdList, long startTime, long endTime) {
        Criteria criteria = new Criteria();

        if (!(startTime > 0 || endTime > 0)) {
            criteria = criteria.andOperator(Criteria.where("contentId").is(contentId),
                    Criteria.where("userId").in(userIdList));
        } else {
            if (startTime > 0 && endTime > 0) {
                criteria = criteria.andOperator(Criteria.where("contentId").is(contentId),
                        Criteria.where("userId").in(userIdList), Criteria.where("createTime").gte(startTime),
                        Criteria.where("createTime").lte(endTime));
            } else if (endTime > 0) {
                criteria = criteria.andOperator(Criteria.where("contentId").is(contentId),
                        Criteria.where("userId").in(userIdList), Criteria.where("createTime").lte(endTime));
            } else {
                criteria = criteria.andOperator(Criteria.where("contentId").is(contentId),
                        Criteria.where("userId").in(userIdList), Criteria.where("createTime").gte(startTime));
            }
        }

        Aggregation agg = Aggregation.newAggregation(

                Aggregation.match(criteria), Aggregation.group("userId").first("userId").as("userId")
                        .first("contentId").as("contentId").count().as("amount")

        );

        List<ContentRecordSummer> contentRecordSummer = mongoTemplate
                .aggregate(agg, TABLE_NAME, ContentRecordSummer.class).getMappedResults();

        if (contentRecordSummer != null && contentRecordSummer.size() > 0) {
            Aggregation minTimeAgg = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("contentId").is(contentId)
                            .andOperator(Criteria.where("userId").in(userIdList))),
                    Aggregation.group("userId").first("userId").as("userId").min("createTime").as("createTime"));

            List<ContentRecordSummer> minTimeContentRecordSummer = mongoTemplate
                    .aggregate(minTimeAgg, TABLE_NAME, ContentRecordSummer.class).getMappedResults();

            Map<String, List<ContentRecordSummer>> map = minTimeContentRecordSummer.stream()
                    .collect(Collectors.groupingBy(ContentRecordSummer::getUserId));

            contentRecordSummer.forEach(record -> {
                if (map.keySet().contains(record.getUserId())) {
                    record.setCreateTime(map.get(record.getUserId()).get(0).getCreateTime());
                }
            });

        }

        return contentRecordSummer;


    }
}
