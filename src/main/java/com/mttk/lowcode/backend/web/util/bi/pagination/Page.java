package com.mttk.lowcode.backend.web.util.bi.pagination;

public class Page {
	//Current page,start from 1
	private final int page;
	//Page size
	private final int size;
	//Total count,-1 means not set
	private  long total=-1;
	//
	public Page() {
		this(1,10);
	}
	public Page(int page, int size) {
		if (page < 0) {
			throw new IllegalArgumentException("Page index must not be less than zero!");
		}
		if (size < 1) {
			throw new IllegalArgumentException("Page size must not be less than one!");
		}

		this.page = page;
		this.size=size;
	}
	public Page(int page, int size,long total) {
		this(page,size);
		this.total=total;
	}
	
	public int getPage() {
		return page;
	}
	public int getSize() {
		return size;
	}
	public long getTotal() {
		return total;
	}

	
	public void setTotal(long total) {
		this.total = total;
	}
	//Calculate offset
	public long getOffset() {
		return (long) (page-1) * (long) size;
	}
	//Total page
	public long getTotalPage() {
		if (size<=0||total<=0) {
			return 0;
		}
		//
		long totalPage=0;
		if(total%size==0){
			totalPage=total/size;
		}else {
			totalPage=total/size+1;
		}
		//
		return totalPage;
	}
	@Override
	public String toString() {
		return "Page [page=" + page + ", size=" + size + ", total=" + total + "]";
	}
	
}
