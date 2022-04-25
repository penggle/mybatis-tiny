package com.penglecode.codeforce.mybatistiny.examples.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.penglecode.codeforce.common.util.DateTimeUtils;
import com.penglecode.codeforce.common.util.JsonUtils;
import com.penglecode.codeforce.mybatistiny.examples.domain.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品示例数据工具类
 *
 * @author pengpeng
 * @version 1.0
 */
public class ProductExampleUtils {

    private static final Random RANDOM = new Random();

    public static List<ProductAggregate> getExampleProductList() {
        try {
            Resource sampleResource = new ClassPathResource("product-example-list.json");
            String json = StreamUtils.copyToString(sampleResource.getInputStream(), StandardCharsets.UTF_8);
            List<ProductExample> productExampleList = JsonUtils.json2Object(json, new TypeReference<List<ProductExample>>() {});
            return productExampleList.stream().map(ProductExampleUtils::buildProduct).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    protected static ProductAggregate buildProduct(ProductExample sample) {
        String nowTime = DateTimeUtils.formatNow();
        ProductAggregate product = new ProductAggregate();
        product.setProductName(sample.getProductName());
        product.setProductType(sample.getProductType());
        product.setProductUrl(sample.getProductUrl());
        product.setProductTags(sample.getProductTags());
        product.setShopId(sample.getShopId());
        product.setAuditStatus(RANDOM.nextInt(3));
        product.setOnlineStatus(RANDOM.nextInt(2));
        product.setCreateTime(nowTime);
        product.setUpdateTime(nowTime);

        ProductExtraInfo productExtra = new ProductExtraInfo();
        productExtra.setProductDetails(sample.getProductUrl());
        productExtra.setProductSpecifications("商品规格");
        productExtra.setProductServices("正品行货，七天包退换");
        productExtra.setCreateTime(nowTime);
        productExtra.setUpdateTime(nowTime);
        product.setProductExtra(productExtra);

        List<List<ProductSaleSpec>> groupedProductSaleSpecs = new ArrayList<>();
        for(Map.Entry<Integer,List<String>> entry : sample.getProductSaleSpecs().entrySet()) {
            List<ProductSaleSpec> subProductSaleSpecs = new ArrayList<>();
            Integer index = entry.getKey();
            List<String> specNames = entry.getValue();
            int specSize = specNames.size();
            for(int i = 1; i <= specSize; i++) {
                ProductSaleSpec productSaleSpec = new ProductSaleSpec();
                productSaleSpec.setSpecNo(index + StringUtils.leftPad(String.valueOf(i), 2, "0"));
                productSaleSpec.setSpecName(specNames.get(i - 1));
                productSaleSpec.setSpecIndex(i);
                productSaleSpec.setCreateTime(nowTime);
                productSaleSpec.setUpdateTime(nowTime);
                subProductSaleSpecs.add(productSaleSpec);
            }
            groupedProductSaleSpecs.add(subProductSaleSpecs);
        }
        product.setProductSaleSpecs(groupedProductSaleSpecs.stream().flatMap(Collection::stream).collect(Collectors.toList()));

        List<ProductSaleStock> productSaleStocks = new ArrayList<>();
        List<List<ProductSaleSpec>> cartesians = Lists.cartesianProduct(groupedProductSaleSpecs); //笛卡尔积
        for (List<ProductSaleSpec> cartesian : cartesians) {
            StringBuilder specNos = new StringBuilder();
            StringBuilder specNames = new StringBuilder();
            for (int j = 0, len2 = cartesian.size(); j < len2; j++) {
                specNos.append(cartesian.get(j).getSpecNo());
                specNames.append(cartesian.get(j).getSpecName());
                if (j != len2 - 1) {
                    specNos.append(":");
                    specNames.append(":");
                }
            }
            ProductSaleStock productSaleStock = new ProductSaleStock();
            productSaleStock.setSpecNo(specNos.toString());
            productSaleStock.setSpecName(specNames.toString());
            long sellPrice = sample.getAvgSellPrice();
            int delta = (int) (sellPrice * 0.2);
            productSaleStock.setSellPrice(RANDOM.nextBoolean() ? sellPrice + RANDOM.nextInt(delta) : sellPrice - RANDOM.nextInt(delta));
            productSaleStock.setStock(RANDOM.nextInt(1000));
            productSaleStock.setCreateTime(nowTime);
            productSaleStock.setUpdateTime(nowTime);
            productSaleStocks.add(productSaleStock);
        }
        product.setProductSaleStocks(productSaleStocks);

        return product;
    }

}
