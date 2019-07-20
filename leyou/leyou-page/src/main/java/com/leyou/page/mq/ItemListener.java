package com.leyou.page.mq;

import com.leyou.page.service.PageService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ItemListener {
    @Autowired
    private PageService pageService;

    /**
     * 处理insert和update的消息
     *
     * @param spuId
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "search.item.insert.queue", durable = "true"),
            exchange = @Exchange(
                    value = "leyou.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}))
    public void listenInsertOrUpdate(Long spuId) throws Exception {
        if (spuId == null) {
            return;
        }
        // 创建或更新索引
        this.pageService.createHtml(spuId);
    }

    /**
     * 处理delete的消息
     *
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "search.item.delete.queue", durable = "true"),
            exchange = @Exchange(
                    value = "leyou.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = "item.delete"))
    public void listenDelete(Long spuId) {
        if (spuId == null) {
            return;
        }
        // 删除索引
        this.pageService.deleteHtml(spuId);
    }
}
