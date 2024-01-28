package cn.liu.test;
import com.alibaba.fastjson.JSON;

import java.util.*;

public class GroupByCleanIndList {

    public static void main(String[] args) {
        // 假设已经有了 pendingWithdrawInfoList
        List<PendingWithdrawInfo> pendingWithdrawInfoList = new ArrayList<>();

        String jsonStr = "[{\"productName\":\"友邦友享一\",\"co\":\"0986\",\"policyNum\":\"C89898989\",\"insuredIdList\":[\"453342\"],\"insuredNameList\":[\"被保人一\"],\"payeeId\":\"453342\",\"payeeName\":\"被保人一\",\"payeeRole\":\"I\",\"payeeType\":\"MS\",\"payeeAmount\":\"1000.00\",\"cleanIndList\":[\"S05\",\"S03\"]},{\"productName\":\"友邦友享一\",\"co\":\"0986\",\"policyNum\":\"C843445453\",\"insuredIdList\":[\"453342\"],\"insuredNameList\":[\"被保人一\"],\"payeeId\":\"453342\",\"payeeName\":\"被保人一\",\"payeeRole\":\"I\",\"payeeType\":\"TB\",\"payeeAmount\":\"1000.00\",\"cleanIndList\":[\"S05\",\"S03\"]},{\"productName\":\"友邦友享二\",\"co\":\"0986\",\"policyNum\":\"C11455326\",\"insuredIdList\":[\"665685\"],\"insuredNameList\":[\"被保人二\"],\"payeeId\":\"785343\",\"payeeName\":\"领款人一\",\"payeeRole\":\"P\",\"payeeType\":\"TB\",\"payeeAmount\":\"1000.00\",\"cleanIndList\":[\"S02\"]},{\"productName\":\"友邦友享三\",\"co\":\"0986\",\"policyNum\":\"C78773799\",\"insuredIdList\":[\"988777\"],\"insuredNameList\":[\"被保人三\"],\"payeeId\":\"988777\",\"payeeName\":\"被保人三\",\"payeeRole\":\"I\",\"payeeType\":\"TB\",\"payeeAmount\":\"1000.00\",\"cleanIndList\":[\"S03\"]}]";
        pendingWithdrawInfoList = JSON.parseArray(jsonStr, PendingWithdrawInfo.class);
        // 将 PendingWithdrawInfo 对象按照 cleanIndList 中的元素进行分组
        Map<String, PolicyData> groupedData = groupByCleanIndList(pendingWithdrawInfoList);

        // 打印分组后的结果
        for (Map.Entry<String, PolicyData> entry : groupedData.entrySet()) {
            System.out.println("CleanIndList: " + entry.getKey());
            List<Customer> customerList = entry.getValue().getData();
            for (Customer customer : customerList) {
                System.out.println("  PayeeId: " + customer.getPayeeId());
                System.out.println("  PayeeName: " + customer.getPayeeName());
                System.out.println("  PayeeRole: " + customer.getPayeeRole());
                System.out.println("  PayeeType: " + customer.getPayeeType());
                Set<Customer.Policy> policyList = customer.getPolicyList();
                for (Customer.Policy policy : policyList) {
                    System.out.println("    Co: " + policy.getCo());
                    System.out.println("    PolicyNum: " + policy.getPolicyNum());
                }
                // 可以打印或者进行其他操作
            }
            System.out.println();
        }
    }

    private static Map<String, PolicyData> groupByCleanIndList(List<PendingWithdrawInfo> pendingWithdrawInfoList) {
        Map<String, PolicyData> groupedData = new HashMap<>();

        for (PendingWithdrawInfo info : pendingWithdrawInfoList) {
            Set<String> cleanIndList = info.getCleanIndList();

            // 遍历 cleanIndList 中的元素
            for (String cleanInd : cleanIndList) {
                // 如果该元素已经在 Map 中存在，则获取对应的 PolicyData 对象，否则新建一个
                PolicyData policyData = groupedData.computeIfAbsent(cleanInd, k -> {
                    PolicyData newData = new PolicyData();
                    newData.setData(new ArrayList<>()); // 确保 data 不为空
                    return newData;
                });

                // 将当前的 Customer 对象添加到 PolicyData 对象的列表中
                Customer customer = policyData.getData().stream()
                        .filter(c -> c.getPayeeId().equals(info.getPayeeId()))
                        .findFirst()
                        .orElseGet(() -> {
                            Customer newCustomer = new Customer();
                            newCustomer.setPolicyList(new HashSet<>()); // 初始化 policyList
                            newCustomer.setPayeeId(info.getPayeeId());
                            newCustomer.setPayeeName(info.getPayeeName());
                            newCustomer.setPayeeRole(info.getPayeeRole());
                            newCustomer.setPayeeType(info.getPayeeType());
                            policyData.getData().add(newCustomer);
                            return newCustomer;
                        });

                // 将 policyNum 和 co 存入 Policy 对象
                Customer.Policy policy = new Customer.Policy();
                policy.setPolicyNum(info.getPolicyNum());
                policy.setCo(info.getCo());
                customer.getPolicyList().add(policy);

                // 将更新后的 PolicyData 对象放回 Map 中
                groupedData.put(cleanInd, policyData);
            }
        }

        return groupedData;
    }
}

