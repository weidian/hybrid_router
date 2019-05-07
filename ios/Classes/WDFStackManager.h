// MIT License
// -----------

// Copyright (c) 2019 WeiDian Group
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:

// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//
//  WDFStackManager.h
//  hybrid_router
//
//  Created by shazhou on 2019/4/25.
//

#import <Foundation/Foundation.h>
#import "WDFlutterURLRouter.h"

NS_ASSUME_NONNULL_BEGIN

@interface WDFStackManager : NSObject

/**
 初始化

 @param delegate 实现<WDFlutterURLRouterDelegate>的代理
 */
+ (void)setupWithDelegate:(id<WDFlutterURLRouterDelegate>)delegate;

/**
 打开flutter页面

 @param page flutter页面名
 @param params 传给页面的参数
 @param result 页面返回时的回调
 */
+ (void)openFlutterPage:(NSString *)page params:(id)params result:(void (^)(id))result;

@end

NS_ASSUME_NONNULL_END
