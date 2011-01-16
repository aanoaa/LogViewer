//
//  RootViewController.m
//  IRCLogViewer
//
//  Created by Yuni Kunho Kim on 12/27/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "RootViewController.h"
#import "ChatTableViewCell.h"
#import "DetailViewController.h"
#import "JSON.h"
#import "Config.h"

@implementation RootViewController
@synthesize list, channel, year, month, date, epoch;

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad {
    [super viewDidLoad];

    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
	
	isFirstLoad = YES;
	[self initData];
}

-(void)refreshData {
	NSString *baseURL = URL_LOG_VIEW;
	NSString *url;
	if(channel != nil && year != nil && month != nil && date != nil && self.list != nil) {
		NSString *lastTimestamp = [[self.list objectAtIndex:[self.list count]-1] objectAtIndex:1];
		url = [NSString stringWithFormat:@"%@/%@/%@/%@/%@/%@", baseURL, channel, year, month, date, lastTimestamp];
		
		NSString *jsonString = [NSString stringWithContentsOfURL:[NSURL URLWithString:url]
														encoding:NSUTF8StringEncoding
														   error:nil];
		//NSLog(@"[refreshDate] request url = %@, json = %@", url, jsonString);
		NSDictionary *data = [jsonString JSONValue];
		
		if(data != nil && [[data objectForKey:@"result"] isEqualToNumber:[NSNumber numberWithInt:200]]) {		
			NSMutableArray *newList = [[NSMutableArray alloc] initWithArray:self.list];
			[newList addObjectsFromArray:[data objectForKey:@"data"]];
			
			self.list = newList;
			
			[newList release];
			
			[self.tableView reloadData];
			[self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:[self.list count]-1 inSection:0]
								  atScrollPosition:UITableViewScrollPositionNone
										  animated:YES];
		}
	}
}

-(void)initData {
	NSString *baseURL = URL_LOG_VIEW;
	NSString *url;
	if(channel != nil && year != nil && month != nil && date != nil) {
		url = [NSString stringWithFormat:@"%@/%@/%@/%@/%@", baseURL, channel, year, month, date];
		self.title = [NSString stringWithFormat:@"%@ : %@/%@/%@", channel, year, month, date];
	}
	else if(channel != nil && year != nil && month != nil) {
		url = [NSString stringWithFormat:@"%@/%@/%@/%@", baseURL, channel, year, month];
		self.title = [NSString stringWithFormat:@"%@ : %@/%@", channel, year, month];
	}
	else if(channel != nil && year != nil) {
		url = [NSString stringWithFormat:@"%@/%@/%@", baseURL, channel, year];
		self.title = [NSString stringWithFormat:@"%@ : %@", channel, year];
	}
	else if(channel != nil) {
		url = [NSString stringWithFormat:@"%@/%@", baseURL, channel];
		self.title = channel;
	}
	else {
		url = [NSString stringWithFormat:@"%@", baseURL];
		self.title = @"Channel";
	}
	
	NSString *jsonString = [NSString stringWithContentsOfURL:[NSURL URLWithString:url]
													encoding:NSUTF8StringEncoding
													   error:nil];
	NSLog(@"request url = %@, json = %@", url, jsonString);
	NSDictionary *data = [jsonString JSONValue];
	//NSLog(@"data = %@, data.data=%@, data.result=%@", data, [data objectForKey:@"data"], [data objectForKey:@"result"]);
	NSNumber *result = [data objectForKey:@"result"];
	
	if(data != nil && [result isEqualToNumber:[NSNumber numberWithInt:200]]) {
		self.list = [data objectForKey:@"data"];
	}
	else {
		[list release];
		self.list = nil;
	}
	//NSLog(@"list = %@", self.list);
}

-(NSString *)stringTime:(NSString *)timestamp {
	NSDate *time = [NSDate dateWithTimeIntervalSince1970:(NSTimeInterval)[timestamp doubleValue]];
	NSDateFormatter *format = [[NSDateFormatter alloc] init];
	[format setDateFormat:@"HH:mm"];
	//NSLog(@"date = %@", [format stringFromDate:time]);
	
	NSString *dateString = [format stringFromDate:time];
	[format release];
	
	return dateString;
}

-(NSString *)stringDateTime:(NSString *)timestamp {
	NSDate *time = [NSDate dateWithTimeIntervalSince1970:(NSTimeInterval)[timestamp doubleValue]];
	NSDateFormatter *format = [[NSDateFormatter alloc] init];
	[format setDateFormat:@"YYYY/MM/DD HH:mm"];
	//NSLog(@"date = %@", [format stringFromDate:time]);
	
	NSString *dateString = [format stringFromDate:time];
	[format release];
	
	return dateString;
}

-(void)setUILabelTextWithVerticalAlignTop:(NSString *)theText label:(UILabel*)label labelSize:(CGSize)labelSize {
	
	CGSize theStringSize = [theText sizeWithFont:label.font
							   constrainedToSize:labelSize
								   lineBreakMode:label.lineBreakMode];
	
	label.frame = CGRectMake(label.frame.origin.x, label.frame.origin.y, theStringSize.width, theStringSize.height);
	label.text = theText;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
	
	if(isFirstLoad) {
		[self.tableView reloadData];
		[self.tableView scrollToRowAtIndexPath:[NSIndexPath indexPathForRow:[self.list count]-1 inSection:0]
							  atScrollPosition:UITableViewScrollPositionNone
									  animated:YES];
		isFirstLoad = NO;
	}
}


/*
- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
}
*/
/*
- (void)viewWillDisappear:(BOOL)animated {
	[super viewWillDisappear:animated];
}
*/
/*
- (void)viewDidDisappear:(BOOL)animated {
	[super viewDidDisappear:animated];
}
*/

/*
 // Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
	// Return YES for supported orientations.
	return (interfaceOrientation == UIInterfaceOrientationPortrait);
}
 */


#pragma mark -
#pragma mark Table view data source

// Customize the number of sections in the table view.
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}


// Customize the number of rows in the table view.
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
	if(list != nil) {
		//NSLog(@"numberOfRowsInSection : %d", [list count]);
		if(date != nil) return [list count] + 1;
		else return [list count];
	}
	else {
		return 0;
	}
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"Cell";
	static NSString *ChatCellIdentifier = @"ChatCell";
	static NSString *MoreCellIdentifier = @"MoreCell";
    
	if(self.date != nil) {
		if(indexPath.row == [list count]) {
			UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:MoreCellIdentifier];
			if (cell == nil) {
				cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:MoreCellIdentifier] autorelease];
			}
			
			cell.textLabel.text = @"Refresh";
			cell.textLabel.textAlignment = UITextAlignmentCenter;
			
			return cell;
		}
		else {
			ChatTableViewCell *cell = (ChatTableViewCell*)[tableView dequeueReusableCellWithIdentifier:ChatCellIdentifier];
			if (cell == nil) {
				NSArray *arr = [[NSBundle mainBundle] loadNibNamed:@"ChatTableViewCell" owner:nil options:nil];
				cell = [arr objectAtIndex:0];
			}
			
			NSArray *msg = [list objectAtIndex:indexPath.row];
			cell.nick.text = [msg objectAtIndex:0];
			cell.datetime.text = [self stringTime:[msg objectAtIndex:1]];
			cell.talk.text = [msg objectAtIndex:2];
			
			[self setUILabelTextWithVerticalAlignTop:cell.talk.text label:cell.talk labelSize:CGSizeMake(188, [self heightForRowAtIndexPath:indexPath])];
			
			return cell;
		}
	}
	else {
		UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
		if (cell == nil) {
			cell = [[[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier] autorelease];
		}
		
		// Configure the cell.
		if([self.title isEqualToString:@"Channel"]) {
			cell.textLabel.text = [NSString stringWithFormat:@"#%@", [[list objectAtIndex:indexPath.row] objectAtIndex:0]];
		}
		else {
			cell.textLabel.text = [[list objectAtIndex:indexPath.row] objectAtIndex:0];
		}
		
		return cell;
	}
}


/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/


/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source.
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view.
    }   
}
*/


/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/


/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/


#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	
	// On Select Refresh
	if(indexPath.row == [list count]) {
		//[self initData];
		//[tableView reloadData];
		[self refreshData];
		return;
	}
	// On Select Talk
	else if(self.date != nil) {
		//[tableView deselectRowAtIndexPath:indexPath animated:YES];
		
		DetailViewController *viewController = [[DetailViewController alloc] initWithNibName:@"DetailViewController" bundle:nil];
		
		NSArray *msg = [list objectAtIndex:indexPath.row];
		NSLog(@"msg : %@", msg);
		viewController.nick = [msg objectAtIndex:0];
		viewController.datetime = [self stringDateTime:[msg objectAtIndex:1]];
		viewController.talk = [msg objectAtIndex:2];
		
		NSLog(@"detailViewController : %@", viewController.nick);

		[self.navigationController pushViewController:viewController animated:YES];
		[viewController release];
		return;
	}
	
	// ------------------------------------------------------------------------
    
	RootViewController *viewController = [[RootViewController alloc] initWithNibName:@"RootViewController" bundle:nil];

	// On Select Channel
	if(self.channel == nil) {
		NSLog(@"1 channel = %@", [[list objectAtIndex:indexPath.row] objectAtIndex:0]);
		viewController.channel = [[list objectAtIndex:indexPath.row] objectAtIndex:0];
	}
	// On Select Year
	else if(self.channel != nil && self.year == nil) {
		NSLog(@"2 channel = %@, year = %@", self.channel, [[list objectAtIndex:indexPath.row] objectAtIndex:0]);
		viewController.channel = self.channel;
		viewController.year = [[list objectAtIndex:indexPath.row] objectAtIndex:0];
	}
	// On Select Month
	else if(self.channel != nil && self.year != nil && self.month == nil) {
		NSLog(@"3");
		viewController.channel = self.channel;
		viewController.year = self.year;
		viewController.month = [[list objectAtIndex:indexPath.row] objectAtIndex:0];
	}
	// On Select Date
	else if(self.channel != nil && self.year != nil && self.month != nil && self.date == nil) {
		NSLog(@"4");
		viewController.channel = self.channel;
		viewController.year = self.year;
		viewController.month = self.month;
		viewController.date = [[list objectAtIndex:indexPath.row] objectAtIndex:0];
	}

	[self.navigationController pushViewController:viewController animated:YES];	
	[viewController release];
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
	return [self heightForRowAtIndexPath:indexPath];
}

- (CGFloat)heightForRowAtIndexPath:(NSIndexPath *)indexPath {
	if(self.date != nil) {
		if(indexPath.row < [list count]) {
			CGSize withinSize = CGSizeMake(188, FLT_MAX); 
			CGSize size = [[[list objectAtIndex:indexPath.row] objectAtIndex:2]
						   sizeWithFont:[UIFont systemFontOfSize:14.0f]
						   constrainedToSize:withinSize
						   lineBreakMode:UILineBreakModeWordWrap];
			return MAX(25.0, size.height + 5);
		}
		else {
			return 50.0;
		}
	}
	else {
		return 50.0;
	}
}	

#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}


- (void)dealloc {
	[list release];
	[channel release];
	[year release];
	[month release];
	[date release];
	[epoch release];
    [super dealloc];
}


@end

