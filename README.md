# 具有Steam风格的尖塔商人
为尖塔商人添加限时特卖活动(参见限时特卖规则). 同时通常价格也有更多折扣(不一定力度大, 但是折扣数量多).  
(跟Steam无关) 增加商人的售卖物和售卖策略: 商人现在额外出售卡牌升级服务和三选一卡牌掉落. 无色卡价格上涨25%(原来是20%), 同时高进阶(>15)卡牌涨价20%, 遗物和药水涨价15%.  
另外, 商人现在会将遗物和药水打包出售! 套包包含两件物品(遗物或药水), 价格比物品单价之和便宜20%!  

## 你必须知道的事
**使用此模组将导致所有涉及商店的其他模组失效**. 这是因为我使用补丁**替换**了原版商店界面的**所有**在`AbstractDungeon`中调用的方法(我很抱歉, 但是我只能想到这么做). 同时, 由于我没有重写原版`FShopScreen`中的updateControllerInput方法, 这极可能导致所有使用控制器的玩家无法使用此模组.  

## 限时特卖规则
日期依2024年steam大促计算比例,将一整年映射到游戏时间48分钟计算周期.  
> 2024年Steam特卖数据:  
> 春促:3月14-21  
> 夏促:6月27-7月11  
> 秋促:11月27-12月4  
> 冬促:12月9-1月2日  

从时间轴看(特卖最后一天也算成完整的一天):  
(1/1-1/2)-(3/14-3/21)-(6/27-7/11)-(11/27-12/4)-(12/9-12/31)  
间隔日期:(2)-72-(8)-96-(15)-138-(8)-4-(22)  
换算(单位秒):总计时间周期:2880  
(15.78)+568.11+(63.12)+757.48+(118.36)+1088.88+(63.12)+31.56+(173.59)

## 一些小问题
为了在商人小小地毯上放下所有东西, 我调整了商人的物品布局. 我必须使用比原版游戏更小的卡牌图像, 这使得我无法渲染卡牌的Tip(天知道为什么`AbstractCard`的相关变量为什么是私有的).  
另外, 商人的倒计时时钟在进入下一个时段前后可能无法正确显示时间, 大致差距不超过2s.  

# 模组架构
由于原版商店相关逻辑比较混乱(指商品架构中卡牌和遗物处理方法完全不同, 另外由商人创建卡牌list有点无法令人信服), 我基于原版`StoreRelic`和`StorePotion`抽象出了`AbstractGoods`作为商人所有可售卖物的父类. 完全使用slot进行商人地毯区域的网格划分, 同时完全使用Hitbox作为玩家选择物品的判定方式(原版的移除卡牌服务没有使用Hitbox). 将商品价格扩展为售价, 原价和折扣三个变量.  
`AbstractGoods`仅负责处理价格相关逻辑和价格牌的渲染, 具体的售卖物的逻辑和渲染均由子类完成(从这个角度讲可能使用继承并不合理). 子类均拥有update, purchase, render方法, 不过我没有额外使用接口类(大概是因为货品少吧).  

# Issues and PRs
非常欢迎, 毕竟我实在是Java新手. 同时欢迎任何关于模组的建议.  