package vip.linhs.stock.service.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import vip.linhs.stock.dao.DailyIndexDao;
import vip.linhs.stock.dao.StockDao;
import vip.linhs.stock.dao.StockLogDao;
import vip.linhs.stock.model.po.DailyIndex;
import vip.linhs.stock.model.po.StockInfo;
import vip.linhs.stock.model.po.StockLog;
import vip.linhs.stock.parser.DailyIndexParser;
import vip.linhs.stock.service.StockCrawlerService;
import vip.linhs.stock.service.StockService;
import vip.linhs.stock.util.StockConsts;
import vip.linhs.stock.util.StockUtil;

@Service
public class StockServiceImpl implements StockService {

    private final Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);

    private static final String LIST_MESSAGE = "'list' must not be null";

    @Autowired
    private StockDao stockDao;

    @Autowired
    private StockLogDao stockLogDao;

    @Autowired
    private DailyIndexDao dailyIndexDao;

    @Autowired
    private StockCrawlerService stockCrawlerService;

    @Autowired
    private DailyIndexParser dailyIndexParser;

    @Override
    public List<StockInfo> getAll() {
        return stockDao.getAll();
    }

    @Override
    public List<StockInfo> getAllListed() {
        return getAll().stream().filter(stockInfo ->
            stockInfo.getState() != StockConsts.StockState.Delisted.value()
                && stockInfo.getState() != StockConsts.StockState.Terminated.value()
                && !StockUtil.isCompositeIndex(stockInfo.getExchange(), stockInfo.getCode())
        ).collect(Collectors.toList());
    }

    @Override
    public void add(List<StockInfo> list) {
        Assert.notNull(list, StockServiceImpl.LIST_MESSAGE);
        if (!list.isEmpty()) {
            stockDao.add(list);
        }
    }

    @Override
    public void update(List<StockInfo> list) {
        Assert.notNull(list, StockServiceImpl.LIST_MESSAGE);
        if (!list.isEmpty()) {
            stockDao.update(list);
        }
    }

    @Override
    public void addStockLog(List<StockLog> list) {
        Assert.notNull(list, StockServiceImpl.LIST_MESSAGE);
        if (!list.isEmpty()) {
            stockLogDao.add(list);
        }
    }

    @Transactional(readOnly = false, rollbackFor = Exception.class)
    @Override
    public void update(List<StockInfo> needAddedList, List<StockInfo> needUpdatedList, List<StockLog> stockLogList) {
        if (needAddedList != null) {
            add(needAddedList);
        }
        if (needUpdatedList != null) {
            update(needUpdatedList);
        }
        if (stockLogList != null) {
            addStockLog(stockLogList);
        }
    }

    @Override
    public void saveDailyIndexToFile(String rootPath) {
        List<StockInfo> list = getAll().stream().filter(stockInfo ->
            !StockUtil.isCompositeIndex(stockInfo.getExchange(), stockInfo.getCode())
        ).collect(Collectors.toList());

        File root = new File(rootPath);

        list.forEach(stockInfo -> {
            logger.info("start save {}: {}", stockInfo.getName(), stockInfo.getCode());
            try {
                File file = new File(root, stockInfo.getExchange() + "/" + stockInfo.getCode() + ".txt");
                if (file.length() < 5 * 1024) {
                    String content = stockCrawlerService.getHistoryDailyIndexsString(stockInfo.getCode());
                    try (FileWriter out = new FileWriter(file)) {
                        FileCopyUtils.copy(content, out);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public void saveDailyIndexFromFile(String rootPath) {
        List<StockInfo> list = getAll().stream().filter(stockInfo ->
            !StockUtil.isCompositeIndex(stockInfo.getExchange(), stockInfo.getCode())
        ).collect(Collectors.toList());

        File root = new File(rootPath);

        list.forEach(stockInfo -> {
            logger.info("start save {}: {}", stockInfo.getName(), stockInfo.getCode());
            try {
                File file = new File(root, stockInfo.getExchange() + "/" + stockInfo.getCode() + ".txt");
                try (FileReader in = new FileReader(file)) {
                    String content = FileCopyUtils.copyToString(in);
                    List<DailyIndex> dailyIndexList = dailyIndexParser.parseHistoryDailyIndexList(content);
                    dailyIndexList.forEach(dailyIndex -> dailyIndex.setStockInfoId(stockInfo.getId()));
                    dailyIndexDao.save(dailyIndexList);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public void saveDailyIndex(DailyIndex dailyIndex) {
        dailyIndexDao.save(dailyIndex);
    }

}
