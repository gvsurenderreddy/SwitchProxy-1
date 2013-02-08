var TableEditor = {

	createTable : function(rows, cols) {
		var buffer = '<table border="1"><tbody>';
		for(i = 0; i < rows; i++) {
			buffer += TableEditor.createRow(cols);
		}
		buffer += '</tbody></table>';
		
		return buffer;
		
	},
	createRow : function(cols) {
		buffer = '';
		buffer += '<tr>';
		for(j = 0; j < cols; j++) {
			buffer += '<td>&nbsp;</td>';
		}
		buffer += '</tr>';
		
		return buffer;
	},
	
	
	findCell : function(node) {
		try {
			do {
				node = node.parentNode;
				
				if(node.nodeName == 'TD') {
					return node;
				}
			}
			while(node != null);
			
			return null;
		}
		catch(e) {
			return null;
		}
		
	},
	
	colCount : function(node) {
		return $(node).parent().children().size();		
	},
	rowCount : function(node) {
		return $(node).parents('tbody').children().size();		
	},	
	
	//----------------------------------------------------------------------
	
	addRow : function(node) {
		var cell = TableEditor.findCell(node);
		if(cell == null) return;
		
		var lastRow = $(cell).parents('tbody').children(':last');
		lastRow.after(TableEditor.createRow(TableEditor.colCount(cell)))
	},
	addColumn : function(node) {
		var cell = TableEditor.findCell(node);
		if(cell == null) return;
		
		$(cell).parents('tbody').children().each(function() {
			$(this).children('td:last').after('<td>&nbsp;</td>')
		});

	},
	insertRow : function(node) {
		var cell = TableEditor.findCell(node);
		if(cell == null) return;

		var currentRow = $(cell).parents('tr')
		currentRow.before(TableEditor.createRow(TableEditor.colCount(cell)))		
	},
	insertColumn : function(node) {
		var cell = TableEditor.findCell(node);
		if(cell == null) return;

		var cellIdx = -1;
		var cells = $(cell).parent().children();
		for(i = 0; i < cells.size(); i++) {
			if(cells[i] == cell) {
				cellIdx = i;
				break;
			}
		}
		
		$(cell).parents('tbody').children().each(function() {
//			alert('td:gt(' + cellIdx + ')');
			$(this).children('td:eq(' + cellIdx + ')').before('<td>&nbsp;</td>');
		});
	}
	
};

