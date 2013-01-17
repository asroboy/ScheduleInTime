package fr.esipe.oc3.km.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class EventParcelable implements Parcelable{

	private String formationId;
	private Date startTime;
	private Date endTime;
	private List<String> labels = new ArrayList<String>();

	public EventParcelable(String formationId, List<String> labels, Date startTime, Date endTime) {
		this.formationId = formationId;
		this.labels = labels;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public EventParcelable() {
	}
	
	public EventParcelable(Parcel in) {
		readFromParcel(in);
	}
	
	public void setFormationId(String formationId) {
        this.formationId = formationId;
    }

    public String getFormationId() {
        return formationId;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void addLabel(String label) {
        labels.add(label);
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<String> getLabels() {
        return labels;
    }
	
	public void readFromParcel(Parcel in) {
		formationId = in.readString();
		in.readList(labels, null);
		startTime = new Date(Integer.parseInt(in.readString()));
		endTime = new Date(Integer.parseInt(in.readString()));
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(formationId);
		dest.writeList(labels);
		dest.writeString(String.valueOf(startTime.getTime()));
		dest.writeString(String.valueOf(endTime.getTime()));

	}

	public static final Parcelable.Creator<EventParcelable> CREATOR =
			new Parcelable.Creator<EventParcelable>() {
		public EventParcelable createFromParcel(Parcel in) {
			return new EventParcelable(in);
		}

		public EventParcelable[] newArray(int size) {
			return new EventParcelable[size];
		}
	};

}
