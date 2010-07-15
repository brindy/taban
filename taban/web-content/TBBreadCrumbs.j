
@import <Foundation/CPObject.j>
@import <Foundation/CPArray.j>

@implementation TBBreadCrumbs : CPView
{

	id _delegate;
	
	CPArray _crumbs;
	
	CPTextField _label;
	
}

-(id) init {
	self = [super initWithFrame:CGRectMake(50, 55, 100, 20)];
	
	if (self) {
		_crumbs = [[CPArray alloc] init];
		
		_label = [[CPTextField alloc] initWithFrame:CGRectMakeZero()];
	    [_label setFont:[CPFont boldSystemFontOfSize:24.0]];
		[_label setStringValue:@"Path: "];
		[_label sizeToFit];
		[self addSubview:_label];
		
	}
	
	return self;
}

-(id) initWithTitle:(CPString) title delegate:(id) theDelegate {
	
	    self = [self init];
		if (self) {
			_delegate = theDelegate;
			[self push:title];
	    }
	 
		return self;
}

-(void) push:(CPString) title {

    var crumb = [[CPButton alloc] initWithFrame:CGRectMakeZero()];
	// [crumb setDelegate:self];
    [crumb setTitle:title];
    [crumb setFont:[CPFont boldSystemFontOfSize:24.0]];
	[crumb sizeToFit];
	
	// underline all the existing crumbs and calculate widths

	var width = [_label frame].size.width + 5;
	for (var i = 0; i < [_crumbs count]; i++) {
		
		var prevCrumb = [_crumbs objectAtIndex:i];
		width += [[[prevCrumb frame] size] width] + 10;
		
		/// TODO linkify it
		
	}
	
	[_crumbs addObject:crumb];
	
	var frame = [crumb frame];
	frame.origin.x = width;
	frame.origin.y = 5;
	[crumb setFrame:frame];
	
	width += frame.size.width;
	
	[self addSubview:crumb];
	frame = [self frame];
	
	frame.size.width = width;
	frame.size.height = [crumb frame].size.height + 5;
	
	[self setFrame:frame];
	
}