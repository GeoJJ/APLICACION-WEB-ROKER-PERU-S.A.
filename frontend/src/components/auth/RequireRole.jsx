import {Outlet} from 'react-router-dom';
export default function RequireRole(){
 // The current backend has no login endpoint. These routes expose views, not authenticated sessions.
 return <Outlet/>;
}
