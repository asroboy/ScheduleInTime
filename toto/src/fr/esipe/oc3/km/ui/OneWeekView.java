package fr.esipe.oc3.km.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import fr.esipe.oc3.km.R;

public class OneWeekView extends Fragment{

	private ArrayList<EventParcelable> eventParce;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* Getting args from bundle */
		Bundle data = getArguments();
		eventParce = data.getParcelableArrayList("events");
	}


	public class MyExpandableListAdapter extends BaseExpandableListAdapter {

		private String[] groups;
		private SparseArray<Vector<Vector<String>>> weekEvents;


		/**
		 * Initialize groups and save all events from a week in e SparseArray
		 * All event in a day are save in Vector save in weekEvents
		 * @param eventParceable
		 */
		public MyExpandableListAdapter(List<EventParcelable> eventParceable) {
			Calendar cal = Calendar.getInstance();
			groups = new String[5];
			boolean firstInitGroup = true;
			Vector<String> mchildren;
			Vector<Vector<String>> mondayEvent = new Vector<Vector<String>>();
			Vector<Vector<String>> tuesdayEvent = new Vector<Vector<String>>();
			Vector<Vector<String>> wednesdayEvent = new Vector<Vector<String>>();
			Vector<Vector<String>> thursdayEvent = new Vector<Vector<String>>();
			Vector<Vector<String>> fridayEvent = new Vector<Vector<String>>();
			weekEvents = new SparseArray<Vector<Vector<String>>>();

			cal.setFirstDayOfWeek(Calendar.MONDAY);

			for(EventParcelable ev : eventParceable){
				cal.setTime(ev.getStartTime());
				if(firstInitGroup) {
					
					cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					cal.add(Calendar.DAY_OF_YEAR, 7);
					groups[Calendar.MONDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
					cal.setTime(ev.getStartTime());

					cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
					cal.add(Calendar.DAY_OF_YEAR, 7);
					groups[Calendar.TUESDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
					cal.setTime(ev.getStartTime());

					cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
					cal.add(Calendar.DAY_OF_YEAR, 7);
					groups[Calendar.WEDNESDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
					cal.setTime(ev.getStartTime());

					cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
					cal.add(Calendar.DAY_OF_YEAR, 7);
					groups[Calendar.THURSDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
					cal.setTime(ev.getStartTime());

					cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
					cal.add(Calendar.DAY_OF_YEAR, 7);
					groups[Calendar.FRIDAY - 2] = (String) android.text.format.DateFormat.format("EEEE"+ " d " + "MMMM", cal);
					cal.setTime(ev.getStartTime());
					firstInitGroup = false;
				}


				mchildren = new Vector<String>();
				mchildren.add((String) android.text.format.DateFormat.format("kk"+ "'h'" + "mm", cal));
				cal.setTime(ev.getEndTime());
				mchildren.add((String) android.text.format.DateFormat.format("kk"+ "'h'" + "mm", cal));
				List<String> labels = ev.getLabels();
				for(String label : labels){
					mchildren.add(label);
				}
				switch (cal.get(Calendar.DAY_OF_WEEK)) {
				case Calendar.MONDAY:
					mondayEvent.add(mchildren);
					break;

				case Calendar.TUESDAY:
					tuesdayEvent.add(mchildren);
					break;

				case Calendar.WEDNESDAY:
					wednesdayEvent.add(mchildren);
					break;

				case Calendar.THURSDAY:
					thursdayEvent.add(mchildren);
					break;

				case Calendar.FRIDAY:
					if(mchildren.size() >= 6)
						fridayEvent.add(mchildren);
					break;

				default:
					break;
				}
			}		

			weekEvents.put(Calendar.MONDAY - 2, mondayEvent);
			weekEvents.put(Calendar.TUESDAY - 2, tuesdayEvent);
			weekEvents.put(Calendar.WEDNESDAY - 2, wednesdayEvent);
			weekEvents.put(Calendar.THURSDAY - 2, thursdayEvent);
			weekEvents.put(Calendar.FRIDAY - 2, fridayEvent);
		}

		@Override
		public Object getChild(int positionGroup, int positionChild) {
			return weekEvents.get(positionGroup).get(positionChild);
		}

		@Override
		public long getChildId(int positionGroup, int positionChild) {
			return positionChild;
		}

		/**
		 * Return view
		 */
		@Override
		public View getChildView(int positionGroup, int positionChild, boolean arg2, View arg3,
				ViewGroup arg4) {

			View inflatedView = null;
			Vector<Vector<String>> days = weekEvents.get(positionGroup);
			Vector<String> mchildren = days.get(positionChild);
			if (mchildren.size() == 6) {
				inflatedView = View.inflate(getActivity().getApplicationContext(),
						R.layout.child_layout, null);


				TextView tvStartTime =(TextView)inflatedView.findViewById(R.id.start_time);
				tvStartTime.setText(mchildren.get(0));

				TextView tvEndTime =(TextView)inflatedView.findViewById(R.id.end_time);
				tvEndTime.setText(mchildren.get(1));

				TextView tvSubject =(TextView)inflatedView.findViewById(R.id.subject);
				tvSubject.setText(mchildren.get(2));

				TextView tvTeacher =(TextView)inflatedView.findViewById(R.id.teacher);
				tvTeacher.setText(mchildren.get(3));

				TextView tvClassroom =(TextView)inflatedView.findViewById(R.id.classroom);
				tvClassroom.setText(mchildren.get(5));

				return inflatedView;

			}
			else if (mchildren.size() > 6) {
				//Special view if we have exam or something else like LastProject Reunion
				View inflatedView1 = View.inflate(getActivity().getApplicationContext(),
						R.layout.special_child_layout, null);

				TextView tvStartTime =(TextView)inflatedView1.findViewById(R.id.start_time_spe);
				tvStartTime.setText(mchildren.get(0));

				TextView tvEndTime =(TextView)inflatedView1.findViewById(R.id.end_time_spe);
				tvEndTime.setText(mchildren.get(1));

				TextView tvSubject =(TextView)inflatedView1.findViewById(R.id.subject_spe);
				tvSubject.setText(mchildren.get(2));

				TextView tvTeacher =(TextView)inflatedView1.findViewById(R.id.teacher_spe);
				tvTeacher.setText(mchildren.get(3));

				TextView tvClassroom =(TextView)inflatedView1.findViewById(R.id.classroom_spe);
				tvClassroom.setText(mchildren.get(5));

				TextView tvExamen =(TextView)inflatedView1.findViewById(R.id.type_spe);
				tvExamen.setText(mchildren.get(6));
				return inflatedView1;
			}
			return inflatedView;
		}

		@Override
		public int getChildrenCount(int positionGroup) {
			return weekEvents.get(positionGroup).size();
		}

		@Override
		public Object getGroup(int positionGroup) {
			return groups[positionGroup];
		}

		@Override
		public int getGroupCount() {
			return groups.length;
		}

		@Override
		public long getGroupId(int arg0) {
			return arg0;
		}

		@Override
		public View getGroupView(int positionGroup, boolean arg1, View arg2,
				ViewGroup parent) {
			View inflatedView = View.inflate(getActivity().getApplicationContext(),
					R.layout.groups_layout, null);

			inflatedView.setPadding(100, 42, 0, 42);
			inflatedView.setBackgroundColor(getResources().getColor(R.color.group_background_color));
			TextView tv = (TextView) inflatedView.findViewById(R.id.group);
			tv.setText(groups[positionGroup]);
			return inflatedView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return false;
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		View v = inflater.inflate(R.layout.one_week_layout, container,false);
		ExpandableListView elv = (ExpandableListView) v.findViewById(R.id.list);
		elv.setAdapter(new MyExpandableListAdapter(eventParce));		
		return elv;

	}
}
