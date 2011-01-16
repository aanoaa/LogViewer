//
//  DetailViewController.h
//  IRCLogViewer
//
//  Created by Yuni Kunho Kim on 12/29/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DetailViewController : UIViewController {
	IBOutlet UILabel *nickLabel;
	IBOutlet UILabel *datetimeLabel;
	UIWebView *webView;
	
	NSString *nick;
	NSString *datetime;
	NSString *talk;
}

@property (nonatomic, retain) UILabel *nickLabel;
@property (nonatomic, retain) UILabel *datetimeLabel;
@property (nonatomic, retain) IBOutlet UIWebView *webView;

@property (nonatomic, retain) NSString *nick;
@property (nonatomic, retain) NSString *datetime;
@property (nonatomic, retain) NSString *talk;

- (NSString *)makeHTML;

@end
