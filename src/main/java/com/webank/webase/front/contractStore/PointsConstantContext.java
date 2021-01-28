/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.front.contractStore;


public class PointsConstantContext {
    public static final String BAC001_SOURCE = "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjQ7CgppbXBvcnQgIi4vU2FmZU1hdGguc29sIjsKaW1wb3J0ICIuL1JvbGVzLnNvbCI7CmltcG9ydCAiLi9BZGRyZXNzLnNvbCI7Cgpjb250cmFjdCBJc3N1ZXJSb2xlIHsKICAgIHVzaW5nIFJvbGVzIGZvciBSb2xlcy5Sb2xlOwoKICAgIGV2ZW50IElzc3VlckFkZGVkKGFkZHJlc3MgaW5kZXhlZCBhY2NvdW50KTsKICAgIGV2ZW50IElzc3VlclJlbW92ZWQoYWRkcmVzcyBpbmRleGVkIGFjY291bnQpOwoKICAgIFJvbGVzLlJvbGUgcHJpdmF0ZSBfaXNzdWVyczsKCiAgICBjb25zdHJ1Y3RvciAoKSBpbnRlcm5hbCB7CiAgICAgICAgX2FkZElzc3Vlcihtc2cuc2VuZGVyKTsKICAgIH0KCiAgICBtb2RpZmllciBvbmx5SXNzdWVyKCkgewogICAgICAgIHJlcXVpcmUoaXNJc3N1ZXIobXNnLnNlbmRlciksICJJc3N1ZXJSb2xlOiBjYWxsZXIgZG9lcyBub3QgaGF2ZSB0aGUgSXNzdWVyIHJvbGUiKTsKICAgICAgICBfOwogICAgfQoKICAgIGZ1bmN0aW9uIGlzSXNzdWVyKGFkZHJlc3MgYWNjb3VudCkgcHVibGljIHZpZXcgcmV0dXJucyAoYm9vbCkgewogICAgICAgIHJldHVybiBfaXNzdWVycy5oYXMoYWNjb3VudCk7CiAgICB9CgogICAgZnVuY3Rpb24gYWRkSXNzdWVyKGFkZHJlc3MgYWNjb3VudCkgcHVibGljIG9ubHlJc3N1ZXIgewogICAgICAgIF9hZGRJc3N1ZXIoYWNjb3VudCk7CiAgICB9CgogICAgZnVuY3Rpb24gcmVub3VuY2VJc3N1ZXIoKSBwdWJsaWMgewogICAgICAgIF9yZW1vdmVJc3N1ZXIobXNnLnNlbmRlcik7CiAgICB9CgogICAgZnVuY3Rpb24gX2FkZElzc3VlcihhZGRyZXNzIGFjY291bnQpIGludGVybmFsIHsKICAgICAgICBfaXNzdWVycy5hZGQoYWNjb3VudCk7CiAgICAgICAgZW1pdCBJc3N1ZXJBZGRlZChhY2NvdW50KTsKICAgIH0KCiAgICBmdW5jdGlvbiBfcmVtb3ZlSXNzdWVyKGFkZHJlc3MgYWNjb3VudCkgaW50ZXJuYWwgewogICAgICAgIF9pc3N1ZXJzLnJlbW92ZShhY2NvdW50KTsKICAgICAgICBlbWl0IElzc3VlclJlbW92ZWQoYWNjb3VudCk7CiAgICB9Cn0KCmNvbnRyYWN0IFN1c3BlbmRlclJvbGUgewogICAgdXNpbmcgUm9sZXMgZm9yIFJvbGVzLlJvbGU7CgogICAgZXZlbnQgU3VzcGVuZGVyQWRkZWQoYWRkcmVzcyBpbmRleGVkIGFjY291bnQpOwogICAgZXZlbnQgU3VzcGVuZGVyUmVtb3ZlZChhZGRyZXNzIGluZGV4ZWQgYWNjb3VudCk7CgogICAgUm9sZXMuUm9sZSBwcml2YXRlIF9zdXNwZW5kZXJzOwoKICAgIGNvbnN0cnVjdG9yICgpIGludGVybmFsIHsKICAgICAgICBfYWRkU3VzcGVuZGVyKG1zZy5zZW5kZXIpOwogICAgfQoKICAgIG1vZGlmaWVyIG9ubHlTdXNwZW5kZXIoKSB7CiAgICAgICAgcmVxdWlyZShpc1N1c3BlbmRlcihtc2cuc2VuZGVyKSwgIlN1c3BlbmRlclJvbGU6IGNhbGxlciBkb2VzIG5vdCBoYXZlIHRoZSBTdXNwZW5kZXIgcm9sZSIpOwogICAgICAgIF87CiAgICB9CgogICAgZnVuY3Rpb24gaXNTdXNwZW5kZXIoYWRkcmVzcyBhY2NvdW50KSBwdWJsaWMgdmlldyByZXR1cm5zIChib29sKSB7CiAgICAgICAgcmV0dXJuIF9zdXNwZW5kZXJzLmhhcyhhY2NvdW50KTsKICAgIH0KCiAgICBmdW5jdGlvbiBhZGRTdXNwZW5kZXIoYWRkcmVzcyBhY2NvdW50KSBwdWJsaWMgb25seVN1c3BlbmRlciB7CiAgICAgICAgX2FkZFN1c3BlbmRlcihhY2NvdW50KTsKICAgIH0KCiAgICBmdW5jdGlvbiByZW5vdW5jZVN1c3BlbmRlcigpIHB1YmxpYyB7CiAgICAgICAgX3JlbW92ZVN1c3BlbmRlcihtc2cuc2VuZGVyKTsKICAgIH0KCiAgICBmdW5jdGlvbiBfYWRkU3VzcGVuZGVyKGFkZHJlc3MgYWNjb3VudCkgaW50ZXJuYWwgewogICAgICAgIF9zdXNwZW5kZXJzLmFkZChhY2NvdW50KTsKICAgICAgICBlbWl0IFN1c3BlbmRlckFkZGVkKGFjY291bnQpOwogICAgfQoKICAgIGZ1bmN0aW9uIF9yZW1vdmVTdXNwZW5kZXIoYWRkcmVzcyBhY2NvdW50KSBpbnRlcm5hbCB7CiAgICAgICAgX3N1c3BlbmRlcnMucmVtb3ZlKGFjY291bnQpOwogICAgICAgIGVtaXQgU3VzcGVuZGVyUmVtb3ZlZChhY2NvdW50KTsKICAgIH0KfQoKY29udHJhY3QgU3VzcGVuZGFibGUgaXMgU3VzcGVuZGVyUm9sZSB7CgogICAgZXZlbnQgU3VzcGVuZGVkKGFkZHJlc3MgYWNjb3VudCk7CiAgICBldmVudCBVblN1c3BlbmRlZChhZGRyZXNzIGFjY291bnQpOwoKICAgIGJvb2wgcHJpdmF0ZSBfc3VzcGVuZGVkOwoKICAgIGNvbnN0cnVjdG9yICgpIGludGVybmFsIHsKICAgICAgICBfc3VzcGVuZGVkID0gZmFsc2U7CiAgICB9CgogICAgLyoqCiAgICAgKiBAcmV0dXJuIFRydWUgaWYgdGhlIGNvbnRyYWN0IGlzIHN1c3BlbmRlZCwgZmFsc2Ugb3RoZXJ3aXNlLgogICAgICovCiAgICBmdW5jdGlvbiBzdXNwZW5kZWQoKSBwdWJsaWMgdmlldyByZXR1cm5zIChib29sKSB7CiAgICAgICAgcmV0dXJuIF9zdXNwZW5kZWQ7CiAgICB9CgogICAgLyoqCiAgICAgKiBAZGV2IE1vZGlmaWVyIHRvIG1ha2UgYSBmdW5jdGlvbiBjYWxsYWJsZSBvbmx5IHdoZW4gdGhlIGNvbnRyYWN0IGlzIG5vdCBzdXNwZW5kZWQuCiAgICAgKi8KICAgIG1vZGlmaWVyIHdoZW5Ob3RTdXNwZW5kZWQoKSB7CiAgICAgICAgcmVxdWlyZSghX3N1c3BlbmRlZCwgIlN1c3BlbmRhYmxlOiBzdXNwZW5kZWQiKTsKICAgICAgICBfOwogICAgfQoKICAgIC8qKgogICAgICogQGRldiBNb2RpZmllciB0byBtYWtlIGEgZnVuY3Rpb24gY2FsbGFibGUgb25seSB3aGVuIHRoZSBjb250cmFjdCBpcyBzdXNwZW5kZWQuCiAgICAgKi8KICAgIG1vZGlmaWVyIHdoZW5TdXNwZW5kZWQoKSB7CiAgICAgICAgcmVxdWlyZShfc3VzcGVuZGVkLCAiU3VzcGVuZGFibGU6IG5vdCBzdXNwZW5kZWQiKTsKICAgICAgICBfOwogICAgfQoKICAgIC8qKgogICAgICogQGRldiBDYWxsZWQgYnkgYSBTdXNwZW5kZXIgdG8gc3VzcGVuZCwgdHJpZ2dlcnMgc3RvcHBlZCBzdGF0ZS4KICAgICAqLwogICAgZnVuY3Rpb24gc3VzcGVuZCgpIHB1YmxpYyBvbmx5U3VzcGVuZGVyIHdoZW5Ob3RTdXNwZW5kZWQgewogICAgICAgIF9zdXNwZW5kZWQgPSB0cnVlOwogICAgICAgIGVtaXQgU3VzcGVuZGVkKG1zZy5zZW5kZXIpOwogICAgfQoKICAgIC8qKgogICAgICogQGRldiBDYWxsZWQgYnkgYSBTdXNwZW5kZXIgdG8gdW5TdXNwZW5kLCByZXR1cm5zIHRvIG5vcm1hbCBzdGF0ZS4KICAgICAqLwogICAgZnVuY3Rpb24gdW5TdXNwZW5kKCkgcHVibGljIG9ubHlTdXNwZW5kZXIgd2hlblN1c3BlbmRlZCB7CiAgICAgICAgX3N1c3BlbmRlZCA9IGZhbHNlOwogICAgICAgIGVtaXQgVW5TdXNwZW5kZWQobXNnLnNlbmRlcik7CiAgICB9Cn0KCmNvbnRyYWN0IElCQUMwMDFSZWNlaXZlciB7CiAgICAvKioKICAgICAqIEBub3RpY2UgSGFuZGxlIHRoZSByZWNlaXB0IG9mIGFuIE5GVAogICAgICogQGRldiBUaGUgQkFDMDAxIHNtYXJ0IGNvbnRyYWN0IGNhbGxzIHRoaXMgZnVuY3Rpb24gb24gdGhlIHJlY2lwaWVudAogICAgICovCiAgICBmdW5jdGlvbiBvbkJBQzAwMVJlY2VpdmVkKGFkZHJlc3Mgb3BlcmF0b3IsIGFkZHJlc3MgZnJvbSwgdWludDI1NiB2YWx1ZSwgYnl0ZXMgZGF0YSkKICAgIHB1YmxpYyByZXR1cm5zIChieXRlczQpOwp9Cgpjb250cmFjdCBCQUMwMDFIb2xkZXIgaXMgSUJBQzAwMVJlY2VpdmVyIHsKICAgIGZ1bmN0aW9uIG9uQkFDMDAxUmVjZWl2ZWQoYWRkcmVzcywgYWRkcmVzcywgdWludDI1NiwgYnl0ZXMpIHB1YmxpYyByZXR1cm5zIChieXRlczQpIHsKICAgICAgICByZXR1cm4gdGhpcy5vbkJBQzAwMVJlY2VpdmVkLnNlbGVjdG9yOwogICAgfQp9CgoKLyoqCiAqIEB0aXRsZSBTdGFuZGFyZCBCQUMwMDEgYXNzZXQKICovCmNvbnRyYWN0IEJBQzAwMSBpcyBJc3N1ZXJSb2xlLCBTdXNwZW5kYWJsZSB7CiAgICB1c2luZyBTYWZlTWF0aCBmb3IgdWludDI1NjsKICAgIHVzaW5nIEFkZHJlc3MgZm9yIGFkZHJlc3M7CgogICAgbWFwcGluZyhhZGRyZXNzID0+IHVpbnQyNTYpIHByaXZhdGUgX2JhbGFuY2VzOwogICAgbWFwcGluZyhhZGRyZXNzID0+IG1hcHBpbmcoYWRkcmVzcyA9PiB1aW50MjU2KSkgcHJpdmF0ZSBfYWxsb3dlZDsKICAgIHVpbnQyNTYgcHJpdmF0ZSBfdG90YWxBbW91bnQ7CiAgICBzdHJpbmcgcHJpdmF0ZSBfZGVzY3JpcHRpb247CiAgICBzdHJpbmcgcHJpdmF0ZSBfc2hvcnROYW1lOwogICAgdWludDggcHJpdmF0ZSAgX21pblVuaXQ7CgogICAgLy8gRXF1YWxzIHRvIGBieXRlczQoa2VjY2FrMjU2KCJvbkJBQzAwMVJlY2VpdmVkKGFkZHJlc3MsYWRkcmVzcyx1aW50MjU2LGJ5dGVzKSIpKWAKICAgIGJ5dGVzNCBwcml2YXRlIGNvbnN0YW50IF9CQUMwMDFfUkVDRUlWRUQgPSAweGM3M2QxNmFlOwoKCiAgICBldmVudCBTZW5kKCBhZGRyZXNzIGluZGV4ZWQgZnJvbSwgYWRkcmVzcyBpbmRleGVkIHRvLCB1aW50MjU2IHZhbHVlLCBieXRlcyBkYXRhKTsKICAgIGV2ZW50IEFwcHJvdmFsKCBhZGRyZXNzIGluZGV4ZWQgb3duZXIsIGFkZHJlc3MgaW5kZXhlZCBzcGVuZGVyLCB1aW50MjU2IHZhbHVlKTsKCgogICAgY29uc3RydWN0b3Ioc3RyaW5nIG1lbW9yeSBkZXNjcmlwdGlvbiwgc3RyaW5nIG1lbW9yeSBzaG9ydE5hbWUsIHVpbnQ4IG1pblVuaXQsIHVpbnQyNTYgdG90YWxBbW91bnQpIHB1YmxpYyB7CiAgICAgICAgX2Rlc2NyaXB0aW9uID0gZGVzY3JpcHRpb247CiAgICAgICAgX3Nob3J0TmFtZSA9IHNob3J0TmFtZTsKICAgICAgICBfbWluVW5pdCA9IG1pblVuaXQ7CiAgICAgICAgX2lzc3VlKG1zZy5zZW5kZXIsIHRvdGFsQW1vdW50ICogKDEwICoqIHVpbnQyNTYobWluVW5pdCkpLCAiIik7CiAgICB9CgoKICAgIGZ1bmN0aW9uIHRvdGFsQW1vdW50KCkgcHVibGljIHZpZXcgcmV0dXJucyAodWludDI1NikgewogICAgICAgIHJldHVybiBfdG90YWxBbW91bnQ7CiAgICB9CgogICAgZnVuY3Rpb24gYmFsYW5jZShhZGRyZXNzIG93bmVyKSBwdWJsaWMgdmlldyByZXR1cm5zICh1aW50MjU2KSB7CiAgICAgICAgcmV0dXJuIF9iYWxhbmNlc1tvd25lcl07CiAgICB9CgogICAgLyoqCiAgICAgKiBAZGV2IEZ1bmN0aW9uIHRvIGNoZWNrIHRoZSBhbW91bnQgb2YgYXNzZXRzIHRoYXQgYW4gb3duZXIgYWxsb3dlZCB0byBhIHNwZW5kZXIuCiAgICAgKi8KICAgIGZ1bmN0aW9uIGFsbG93YW5jZShhZGRyZXNzIG93bmVyLCBhZGRyZXNzIHNwZW5kZXIpIHB1YmxpYyB2aWV3IHJldHVybnMgKHVpbnQyNTYpIHsKICAgICAgICByZXR1cm4gX2FsbG93ZWRbb3duZXJdW3NwZW5kZXJdOwogICAgfQoKICAgIGZ1bmN0aW9uIHNlbmQoYWRkcmVzcyB0bywgdWludDI1NiB2YWx1ZSwgYnl0ZXMgZGF0YSkgcHVibGljIHdoZW5Ob3RTdXNwZW5kZWQgewogICAgICAgIF9zZW5kKG1zZy5zZW5kZXIsIHRvLCB2YWx1ZSwgZGF0YSk7CiAgICAgICAgcmVxdWlyZShfY2hlY2tPbkJBQzAwMVJlY2VpdmVkKG1zZy5zZW5kZXIsIHRvLCB2YWx1ZSwgZGF0YSksICJCQUMwMDE6IHNlbmQgdG8gbm9uIEJBQzAwMVJlY2VpdmVyIGltcGxlbWVudGVyIik7CgogICAgfQoKLy8gICAgZnVuY3Rpb24gc2FmZVNlbmQoYWRkcmVzcyB0bywgdWludDI1NiB2YWx1ZSwgYnl0ZXMgZGF0YSkgcHVibGljIHdoZW5Ob3RTdXNwZW5kZWQgewovLyAgICAgICAgc2VuZCh0bywgdmFsdWUsIGRhdGEpOwovLyAgICAgICAgcmVxdWlyZShfY2hlY2tPbkJBQzAwMVJlY2VpdmVkKG1zZy5zZW5kZXIsIHRvLCB2YWx1ZSwgZGF0YSksICJCQUMwMDE6IHNlbmQgdG8gbm9uIEJBQzAwMVJlY2VpdmVyIGltcGxlbWVudGVyIik7Ci8vICAgIH0KCgogICAgLyoqCiAgICAgKiBAZGV2IEFwcHJvdmUgdGhlIHBhc3NlZCBhZGRyZXNzIHRvIHNwZW5kIHRoZSBzcGVjaWZpZWQgYW1vdW50IG9mIGFzc2V0cyBvbiBiZWhhbGYgb2YgbXNnLnNlbmRlci4KICAgICAqLwogICAgZnVuY3Rpb24gYXBwcm92ZShhZGRyZXNzIHNwZW5kZXIsIHVpbnQyNTYgdmFsdWUpIHB1YmxpYyB3aGVuTm90U3VzcGVuZGVkIHJldHVybnMgKGJvb2wpIHsKICAgICAgICBfYXBwcm92ZShtc2cuc2VuZGVyLCBzcGVuZGVyLCB2YWx1ZSk7CiAgICAgICAgcmV0dXJuIHRydWU7CiAgICB9CgogICAgLyoqCiAgICAgKiBAZGV2IFNlbmQgYXNzZXRzIGZyb20gb25lIGFkZHJlc3MgdG8gYW5vdGhlci4KICAgICAqLwogICAgZnVuY3Rpb24gc2VuZEZyb20oYWRkcmVzcyBmcm9tLCBhZGRyZXNzIHRvLCB1aW50MjU2IHZhbHVlLCBieXRlcyBkYXRhKSBwdWJsaWMgd2hlbk5vdFN1c3BlbmRlZCB7CiAgICAgICAgX3NlbmQoZnJvbSwgdG8sIHZhbHVlLCBkYXRhKTsKICAgICAgICBfYXBwcm92ZShmcm9tLCBtc2cuc2VuZGVyLCBfYWxsb3dlZFtmcm9tXVttc2cuc2VuZGVyXS5zdWIodmFsdWUpKTsKICAgICAgICAvL2FkZAogICAgICAgIHJlcXVpcmUoX2NoZWNrT25CQUMwMDFSZWNlaXZlZChmcm9tLCB0bywgdmFsdWUsIGRhdGEpLCAiQkFDMDAxOiBzZW5kIHRvIG5vbiBCQUMwMDFSZWNlaXZlciBpbXBsZW1lbnRlciIpOwoKCiAgICB9CgovLy8vIHNhZmUgdG9kbwovLyAgICBmdW5jdGlvbiBzYWZlU2VuZEZyb20oYWRkcmVzcyBmcm9tLCBhZGRyZXNzIHRvLCB1aW50MjU2IHZhbHVlLCBieXRlcyBkYXRhKSBwdWJsaWMgd2hlbk5vdFN1c3BlbmRlZCB7Ci8vICAgICAgICBzZW5kRnJvbShmcm9tLCB0bywgdmFsdWUsIGRhdGEpOwovLyAgICAgICAgcmVxdWlyZShfY2hlY2tPbkJBQzAwMVJlY2VpdmVkKGZyb20sIHRvLCB2YWx1ZSwgZGF0YSksICJCQUMwMDE6IHNlbmQgdG8gbm9uIEJBQzAwMVJlY2VpdmVyIGltcGxlbWVudGVyIik7Ci8vICAgIH0KCgogICAgZnVuY3Rpb24gYmF0Y2hTZW5kKGFkZHJlc3NbXSB0bywgdWludDI1NltdIHZhbHVlcywgYnl0ZXMgZGF0YSkgcHVibGljIHdoZW5Ob3RTdXNwZW5kZWQgewoKICAgICAgICAvLyBNVVNUIFRocm93IG9uIGVycm9ycwoKICAgICAgICByZXF1aXJlKHRvLmxlbmd0aCA9PSB2YWx1ZXMubGVuZ3RoLCAidG8gYW5kIHZhbHVlcyBhcnJheSBsZW5naHQgbXVzdCBtYXRjaC4iKTsKCiAgICAgICAgZm9yICh1aW50MjU2IGkgPSAwOyBpIDwgdG8ubGVuZ3RoOyArK2kpIHsKICAgICAgICAgICAgcmVxdWlyZSh0b1tpXSAhPSBhZGRyZXNzKDB4MCksICJkZXN0aW5hdGlvbiBhZGRyZXNzIG11c3QgYmUgbm9uLXplcm8uIik7CgogICAgICAgICAgICBzZW5kKHRvW2ldLCB2YWx1ZXNbaV0sIGRhdGEpOwogICAgICAgIH0KICAgIH0KCgogICAgZnVuY3Rpb24gX2NoZWNrT25CQUMwMDFSZWNlaXZlZChhZGRyZXNzIGZyb20sIGFkZHJlc3MgdG8sIHVpbnQyNTYgdmFsdWUsIGJ5dGVzIGRhdGEpCiAgICBpbnRlcm5hbCByZXR1cm5zIChib29sKQogICAgewogICAgICAgIGlmICghdG8uaXNDb250cmFjdCgpKSB7CiAgICAgICAgICAgIHJldHVybiB0cnVlOwogICAgICAgIH0KCiAgICAgICAgYnl0ZXM0IHJldHZhbCA9IElCQUMwMDFSZWNlaXZlcih0bykub25CQUMwMDFSZWNlaXZlZChmcm9tLCB0bywgdmFsdWUsIGRhdGEpOwogICAgICAgIHJldHVybiAocmV0dmFsID09IF9CQUMwMDFfUkVDRUlWRUQpOwogICAgfQoKICAgIC8qKgogICAgICogQGRldiBJbmNyZWFzZSB0aGUgYW1vdW50IG9mIGFzc2V0cyB0aGF0IGFuIG93bmVyIGFsbG93ZWQgdG8gYSBzcGVuZGVyLgogICAgICovCiAgICBmdW5jdGlvbiBpbmNyZWFzZUFsbG93YW5jZShhZGRyZXNzIHNwZW5kZXIsIHVpbnQyNTYgYWRkZWRWYWx1ZSkgcHVibGljIHdoZW5Ob3RTdXNwZW5kZWQgcmV0dXJucyAoYm9vbCkgewogICAgICAgIF9hcHByb3ZlKG1zZy5zZW5kZXIsIHNwZW5kZXIsIF9hbGxvd2VkW21zZy5zZW5kZXJdW3NwZW5kZXJdLmFkZChhZGRlZFZhbHVlKSk7CiAgICAgICAgcmV0dXJuIHRydWU7CiAgICB9CgogICAgLyoqCiAgICAgKiBAZGV2IERlY3JlYXNlIHRoZSBhbW91bnQgb2YgYXNzZXRzIHRoYXQgYW4gb3duZXIgYWxsb3dlZCB0byBhIHNwZW5kZXIuCiAgICAgKiBhcHByb3ZlIHNob3VsZCBiZSBjYWxsZWQgd2hlbiBfYWxsb3dlZFttc2cuc2VuZGVyXVtzcGVuZGVyXSA9PSAwLiBUbyBkZWNyZW1lbnQKICAgICAqLwogICAgZnVuY3Rpb24gZGVjcmVhc2VBbGxvd2FuY2UoYWRkcmVzcyBzcGVuZGVyLCB1aW50MjU2IHN1YnRyYWN0ZWRWYWx1ZSkgcHVibGljIHdoZW5Ob3RTdXNwZW5kZWQgcmV0dXJucyAoYm9vbCkgewogICAgICAgIF9hcHByb3ZlKG1zZy5zZW5kZXIsIHNwZW5kZXIsIF9hbGxvd2VkW21zZy5zZW5kZXJdW3NwZW5kZXJdLnN1YihzdWJ0cmFjdGVkVmFsdWUpKTsKICAgICAgICByZXR1cm4gdHJ1ZTsKICAgIH0KCiAgICBmdW5jdGlvbiBkZXN0cm95KHVpbnQyNTYgdmFsdWUsIGJ5dGVzIGRhdGEpIHB1YmxpYyB7CiAgICAgICAgX2Rlc3Ryb3kobXNnLnNlbmRlciwgdmFsdWUsIGRhdGEpOwogICAgfQoKICAgIC8qKgogICAgICogQGRldiBCdXJucyBhIHNwZWNpZmljIGFtb3VudCBvZiBhc3NldHMgZnJvbSB0aGUgdGFyZ2V0IGFkZHJlc3MgYW5kIGRlY3JlbWVudHMgYWxsb3dhbmNlLgogICAgICovCiAgICBmdW5jdGlvbiBkZXN0cm95RnJvbShhZGRyZXNzIGZyb20sIHVpbnQyNTYgdmFsdWUsIGJ5dGVzIGRhdGEpIHB1YmxpYyB7CiAgICAgICAgX2Rlc3Ryb3lGcm9tKGZyb20sIHZhbHVlLCBkYXRhKTsKICAgIH0KCgogICAgZnVuY3Rpb24gZGVzY3JpcHRpb24oKSBwdWJsaWMgdmlldyByZXR1cm5zIChzdHJpbmcgbWVtb3J5KSB7CiAgICAgICAgcmV0dXJuIF9kZXNjcmlwdGlvbjsKICAgIH0KCiAgICAvKioKICAgICAqIEByZXR1cm4gdGhlIHNob3J0TmFtZSBvZiB0aGUgYXNzZXQuCiAgICAgKi8KICAgIGZ1bmN0aW9uIHNob3J0TmFtZSgpIHB1YmxpYyB2aWV3IHJldHVybnMgKHN0cmluZyBtZW1vcnkpIHsKICAgICAgICByZXR1cm4gX3Nob3J0TmFtZTsKICAgIH0KCiAgICAvKioKICAgICAqIEByZXR1cm4gdGhlIG51bWJlciBvZiBtaW5Vbml0IG9mIHRoZSBhc3NldC4KICAgICAqLwogICAgZnVuY3Rpb24gbWluVW5pdCgpIHB1YmxpYyB2aWV3IHJldHVybnMgKHVpbnQ4KSB7CiAgICAgICAgcmV0dXJuIF9taW5Vbml0OwogICAgfQoKCiAgICBmdW5jdGlvbiBpc3N1ZShhZGRyZXNzIHRvLCB1aW50MjU2IHZhbHVlLCBieXRlcyBkYXRhKSBwdWJsaWMgb25seUlzc3VlciByZXR1cm5zIChib29sKSB7CiAgICAgICAgX2lzc3VlKHRvLCB2YWx1ZSwgZGF0YSk7CiAgICAgICAgcmV0dXJuIHRydWU7CiAgICB9CiAgICAvKioKICAgICAqIEBkZXYgU2VuZCBhc3NldCBmb3IgYSBzcGVjaWZpZWQgYWRkcmVzc2VzLgogICAgICovCiAgICBmdW5jdGlvbiBfc2VuZChhZGRyZXNzIGZyb20sIGFkZHJlc3MgdG8sIHVpbnQyNTYgdmFsdWUsIGJ5dGVzIGRhdGEpIGludGVybmFsIHsKICAgICAgICByZXF1aXJlKHRvICE9IGFkZHJlc3MoMCksICJCQUMwMDE6IHNlbmQgdG8gdGhlIHplcm8gYWRkcmVzcyIpOwoKICAgICAgICBfYmFsYW5jZXNbZnJvbV0gPSBfYmFsYW5jZXNbZnJvbV0uc3ViKHZhbHVlKTsKICAgICAgICBfYmFsYW5jZXNbdG9dID0gX2JhbGFuY2VzW3RvXS5hZGQodmFsdWUpOwogICAgICAgIGVtaXQgU2VuZCggZnJvbSwgdG8sIHZhbHVlLCBkYXRhKTsKICAgIH0KCiAgICAvKioKICAgICAqIEBkZXYgSW50ZXJuYWwgZnVuY3Rpb24gdGhhdCBpc3N1ZXMgYW4gYW1vdW50IG9mIHRoZSBhc3NldCBhbmQgYXNzaWducyBpdCB0bwogICAgICovCiAgICBmdW5jdGlvbiBfaXNzdWUoYWRkcmVzcyBhY2NvdW50LCB1aW50MjU2IHZhbHVlLCBieXRlcyBkYXRhKSBpbnRlcm5hbCB7CiAgICAgICAgcmVxdWlyZShhY2NvdW50ICE9IGFkZHJlc3MoMCksICJCQUMwMDE6IGlzc3VlIHRvIHRoZSB6ZXJvIGFkZHJlc3MiKTsKCiAgICAgICAgX3RvdGFsQW1vdW50ID0gX3RvdGFsQW1vdW50LmFkZCh2YWx1ZSk7CiAgICAgICAgX2JhbGFuY2VzW2FjY291bnRdID0gX2JhbGFuY2VzW2FjY291bnRdLmFkZCh2YWx1ZSk7CiAgICAgICAgZW1pdCBTZW5kKCBhZGRyZXNzKDApLCBhY2NvdW50LCB2YWx1ZSwgZGF0YSk7CiAgICB9CgogICAgLyoqCiAgICAgKiBAZGV2IEludGVybmFsIGZ1bmN0aW9uIHRoYXQgZGVzdHJveXMgYW4gYW1vdW50IG9mIHRoZSBhc3NldCBvZiBhIGdpdmVuCiAgICAgKi8KICAgIGZ1bmN0aW9uIF9kZXN0cm95KGFkZHJlc3MgYWNjb3VudCwgdWludDI1NiB2YWx1ZSwgYnl0ZXMgZGF0YSkgaW50ZXJuYWwgewogICAgICAgIHJlcXVpcmUoYWNjb3VudCAhPSBhZGRyZXNzKDApLCAiQkFDMDAxOiBkZXN0cm95IGZyb20gdGhlIHplcm8gYWRkcmVzcyIpOwoKICAgICAgICBfdG90YWxBbW91bnQgPSBfdG90YWxBbW91bnQuc3ViKHZhbHVlKTsKICAgICAgICBfYmFsYW5jZXNbYWNjb3VudF0gPSBfYmFsYW5jZXNbYWNjb3VudF0uc3ViKHZhbHVlKTsKICAgICAgICBlbWl0IFNlbmQoIGFjY291bnQsIGFkZHJlc3MoMCksIHZhbHVlLCBkYXRhKTsKICAgIH0KCiAgICAvKioKICAgICAqIEBkZXYgQXBwcm92ZSBhbiBhZGRyZXNzIHRvIHNwZW5kIGFub3RoZXIgYWRkcmVzc2VzJyBhc3NldHMuCiAgICAgKi8KICAgIGZ1bmN0aW9uIF9hcHByb3ZlKGFkZHJlc3Mgb3duZXIsIGFkZHJlc3Mgc3BlbmRlciwgdWludDI1NiB2YWx1ZSkgaW50ZXJuYWwgewogICAgICAgIHJlcXVpcmUob3duZXIgIT0gYWRkcmVzcygwKSwgIkJBQzAwMTogYXBwcm92ZSBmcm9tIHRoZSB6ZXJvIGFkZHJlc3MiKTsKICAgICAgICByZXF1aXJlKHNwZW5kZXIgIT0gYWRkcmVzcygwKSwgIkJBQzAwMTogYXBwcm92ZSB0byB0aGUgemVybyBhZGRyZXNzIik7CgogICAgICAgIF9hbGxvd2VkW293bmVyXVtzcGVuZGVyXSA9IHZhbHVlOwogICAgICAgIGVtaXQgQXBwcm92YWwoIG93bmVyLCBzcGVuZGVyLCB2YWx1ZSk7CiAgICB9CgogICAgLyoqCiAgICAgKiBAZGV2IEludGVybmFsIGZ1bmN0aW9uIHRoYXQgZGVzdHJveXMgYW4gYW1vdW50IG9mIHRoZSBhc3NldCBvZiBhIGdpdmVuCiAgICAgKi8KICAgIGZ1bmN0aW9uIF9kZXN0cm95RnJvbShhZGRyZXNzIGFjY291bnQsIHVpbnQyNTYgdmFsdWUsIGJ5dGVzIGRhdGEpIGludGVybmFsIHsKICAgICAgICBfZGVzdHJveShhY2NvdW50LCB2YWx1ZSwgZGF0YSk7CiAgICAgICAgX2FwcHJvdmUoYWNjb3VudCwgbXNnLnNlbmRlciwgX2FsbG93ZWRbYWNjb3VudF1bbXNnLnNlbmRlcl0uc3ViKHZhbHVlKSk7CiAgICB9Cn0KCg==";
    public static final String I_BAC001_SOURCE = "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjQ7CgoKaW50ZXJmYWNlIElCQUMwMDEgewoKICAgIGZ1bmN0aW9uIHRvdGFsQW1vdW50KCkgcHVibGljIHZpZXcgcmV0dXJucyAodWludDI1Nik7CgogICAgZnVuY3Rpb24gYmFsYW5jZShhZGRyZXNzIG93bmVyKSBwdWJsaWMgdmlldyByZXR1cm5zICh1aW50MjU2KTsKCiAgICBmdW5jdGlvbiBzZW5kKGFkZHJlc3MgdG8sIHVpbnQyNTYgdmFsdWUsIGJ5dGVzIGRhdGEpIHB1YmxpYyA7CgogICAgZnVuY3Rpb24gc2VuZEZyb20oYWRkcmVzcyBmcm9tLCBhZGRyZXNzIHRvLCB1aW50MjU2IHZhbHVlLCBieXRlcyBkYXRhKSBwdWJsaWM7CgogICAgZnVuY3Rpb24gYWxsb3dhbmNlKGFkZHJlc3Mgb3duZXIsIGFkZHJlc3Mgc3BlbmRlcikgcHVibGljIHZpZXcgcmV0dXJucyAodWludDI1Nik7CgogICAgZnVuY3Rpb24gYXBwcm92ZShhZGRyZXNzIHNwZW5kZXIsIHVpbnQyNTYgYW1vdW50KSBwdWJsaWMgcmV0dXJucyAoYm9vbCk7CgogICAgZnVuY3Rpb24gZGVzdHJveSh1aW50MjU2IHZhbHVlLCBieXRlcyBkYXRhKSBwdWJsaWM7CgogICAgZnVuY3Rpb24gZGVzdHJveUZyb20oYWRkcmVzcyBmcm9tLCB1aW50MjU2IHZhbHVlLCBieXRlcyBkYXRhKSBwdWJsaWM7CgogICAgZnVuY3Rpb24gaXNzdWUoYWRkcmVzcyB0bywgdWludDI1NiB2YWx1ZSwgYnl0ZXMgZGF0YSkgcHVibGljICByZXR1cm5zIChib29sKTsKCiAgICBmdW5jdGlvbiBiYXRjaFNlbmQoYWRkcmVzc1tdIHRvLCB1aW50MjU2W10gdmFsdWVzLCBieXRlcyBkYXRhKSBwdWJsaWM7CgogICAgZnVuY3Rpb24gaW5jcmVhc2VBbGxvd2FuY2UoYWRkcmVzcyBzcGVuZGVyLCB1aW50MjU2IGFkZGVkVmFsdWUpIHB1YmxpYyAgcmV0dXJucyAoYm9vbCk7CgogICAgZnVuY3Rpb24gZGVjcmVhc2VBbGxvd2FuY2UoYWRkcmVzcyBzcGVuZGVyLCB1aW50MjU2IHN1YnRyYWN0ZWRWYWx1ZSkgcHVibGljICByZXR1cm5zIChib29sKTsKCiAgICBldmVudCBTZW5kKGFkZHJlc3MgaW5kZXhlZCBmcm9tLCBhZGRyZXNzIGluZGV4ZWQgdG8sIHVpbnQyNTYgdmFsdWUsIGJ5dGVzIGRhdGEpOwoKICAgIGV2ZW50IEFwcHJvdmFsKGFkZHJlc3MgaW5kZXhlZCBvd25lciwgYWRkcmVzcyBpbmRleGVkIHNwZW5kZXIsIHVpbnQyNTYgdmFsdWUpOwoKfQ==";
    public static final String BAC001_MD = "# 积分合约\n" +
            "\n" +
            "## 简介\n" +
            " BAC001 是一套区块链积分合约，具有积分相关的增发，销毁，暂停合约，黑白名单等权限控制等功能。\n" +
            "\n" +
            "## 四个基本元素\n" +
            "\n" +
            "- description \n" +
            "\n" +
            "  此积分的具体描述\n" +
            "\n" +
            "- shortName \n" +
            "\n" +
            "  积分简称\n" +
            "\n" +
            "- minUnit \n" +
            "\n" +
            "  积分最小单位\n" +
            "\n" +
            "- totalAmount \n" +
            "\n" +
            "  积分总数量\n" +
            "\n" +
            "## 五个基本行为: \n" +
            "\n" +
            "- 发行\n" +
            "\n" +
            "  调用合约的 deploy 方法，传入你初始化的四个元素即可，即在区块链上发行了你指定总量和名称的积分。\n" +
            "\n" +
            "  - 其中 minUnit 和 totalAmount 不能为负数或小数\n" +
            "\n" +
            "- 转账\n" +
            "\n" +
            "  调用 send 方法即可实现转账，之后调用 balance 方法可以查看自己的积分余额\n" +
            "\n" +
            "- 增发\n" +
            "\n" +
            "  调用 issue 方法特定地址增发积分， 并可以通过 addIssuer 增加有权限增发积分的人，也可以通过renounceIssuer 方法移除增发权限\n" +
            "\n" +
            "- 销毁\n" +
            "\n" +
            "  调用 destory 以及 destoryFrom 销毁自己地址下积分和特定地址下的积分\n" +
            "\n" +
            "- 暂停\n" +
            "\n" +
            "  遇到紧急状况，你可以调用 suspend 方法，暂停合约，这样任何人都不能调用 send 函数。故障修复后，可以调用 unSuspend 方法解除暂停。也可以通过 addSuspender 和 renounceSuspender 相应增加和移除暂停者权限\n" +
            "\n" +
            "\n" +
            "## 接口说明\n" +
            "\n" +
            "- <b>totalAmount()</b>\n" +
            "\n" +
            "  返回积分总量\n" +
            "\n" +
            "  - 这里的积分总量需要计算最小转账单位，所以实际返回值为   totalAmount * 10<sup>minUnit</sup> \n" +
            "\n" +
            "- <b>balance(address owner)</b>\n" +
            "\n" +
            "  返回owner的帐户的积分余额\n" +
            "\n" +
            "- <b>send(address to, uint256 value , string data)</b>\n" +
            "\n" +
            "  将数量为value的积分转入地址 to 并触发 transfer 事件, data 是转账备注\n" +
            "\n" +
            "  - suspend 状态下无法进行此操作\n" +
            "  - 请避免 to 为自身进行操作\n" +
            "\n" +
            "- <b>sendFrom(address from,address to,uint256 value，string  data))</b>\n" +
            "\n" +
            "  将地址 from 中的 value 数量的积分转入地址 to ，并触发 transfer 事件，data 是转账备注。\n" +
            "\n" +
            "  - 方法的调用者可以不为 from， 此时需要预先进行 approve 授权\n" +
            "\n" +
            "  - from 不能为调用者自身地址，否则会报错\n" +
            "  - suspend 状态下无法执行此操作\n" +
            "\n" +
            "- <b>safeSendFrom(address from, address to, uint256 value,  string data)</b>\n" +
            "\n" +
            "  安全的将地址 from 中的 value 数量的积分转入地址 to ( to如果是合约地址，必须实现接收接口 BAC001Holder 才可以接收转账) ，并触发 transfer 事件，data 是转账备注\n" +
            "\n" +
            "  - suspend 状态下无法执行此操作\n" +
            "\n" +
            "- <b>safeBatchSend( address[] to, uint256[]  values, string  data)</b>\n" +
            "\n" +
            "  批量将自己账户下的积分转给 to 数组的地址， to 和 values 的个数要一致\n" +
            "\n" +
            "  - suspend 状态下无法执行此操作\n" +
            "\n" +
            "- <b>approve(address spender,uint256 value)</b>\n" +
            "\n" +
            "  允许 spender 从自己账户提取限额 value 的积分\n" +
            "\n" +
            "  - 此方法配合 sendfrom / safesendfrom 一起使用\n" +
            "  - 重复授权时，最终授权额度为最后一次授权的值\n" +
            "\n" +
            "- <b>allowance(address owner,address spender)</b>\n" +
            "\n" +
            "  返回 spender 可从 owner 提取的积分数量上限\n" +
            "\n" +
            "  - 此方法配合 approve 一起使用\n" +
            "\n" +
            "- <b>increaseAllowance(address spender, uint256 addedValue)</b>\n" +
            "\n" +
            "  允许 spender 提取的积分上限在原有基础上增加 addedValue\n" +
            "\n" +
            "  - 此方法配合 approve 使用\n" +
            "\n" +
            "- <b>decreaseAllowance(address spender, uint256 subtractedValue)</b>\n" +
            "\n" +
            "  允许 spender  提取的积分上限在原有基础上减少 subtractedValue\n" +
            "\n" +
            "  - 此方法配合 approve 使用\n" +
            "\n" +
            "- <b>minUnit()</b>\n" +
            "\n" +
            "  积分最小单位\n" +
            "\n" +
            "- <b>shortName()</b>\n" +
            "\n" +
            "  积分简称\n" +
            "\n" +
            "- <b>description()</b>\n" +
            "\n" +
            "  积分描述\n" +
            "\n" +
            "- <b>destory(uint256 value， string  data)</b>\n" +
            "\n" +
            "  减少自己的积分，data 是转账备注\n" +
            "\n" +
            "  - 调用时，value 值需要小于等于目前自己的积分总量\n" +
            "\n" +
            "- <b>destroyFrom(address from, uint256 value， string  data)</b>\n" +
            "\n" +
            "  减少地址 from 积分，data 是转账备注\n" +
            "\n" +
            "  - 调用此方法时，需要配合 approve 进行使用\n" +
            "\n" +
            "- <b>issue(address to, uint256 value，string  data)</b>\n" +
            "\n" +
            "  给地址 to 增加数量为 value 的积分，data 是转账备注\n" +
            "\n" +
            "- <b>isIssuer(address account)</b>\n" +
            "\n" +
            "  检查 account 是否有增加积分的权限\n" +
            "\n" +
            "- <b>addIssuer(address account)</b>\n" +
            "\n" +
            "  使地址 account 拥有增加积分的权限\n" +
            "\n" +
            "- <b>renounceIssuer()</b>\n" +
            "\n" +
            "  移除增加积分的权限\n" +
            "\n" +
            "- <b>suspend()</b>\n" +
            "\n" +
            "  暂停合约\n" +
            "\n" +
            "  - suspend 后无法进行 send / safesendfrom / sendfrom / safeBatchSend / approves 操作\n" +
            "\n" +
            "- <b>unSuspend()</b>\n" +
            "\n" +
            "  重启合约\n" +
            "  \n" +
            "- <b>suspended</b>\n" +
            "\n" +
            "  判断合约是否处于暂停状态\n" +
            "\n" +
            "- <b>isSuspender(address account)</b>\n" +
            "\n" +
            "  是否有暂停合约权限\n" +
            "\n" +
            "  - 配合 suspend 方法一起使用\n" +
            "\n" +
            "- <b>addSuspender(address account)</b>\n" +
            "\n" +
            "  增加暂停权限者\n" +
            "\n" +
            "  - 配合 suspend 方法一起使用\n" +
            "\n" +
            "- <b>renounceSuspender()</b>\n" +
            "\n" +
            "  移除暂停权限\n" +
            "\n" +
            "  - 配合 suspend / addSuspender 方法使用\n" +
            "\n" +
            "\n";
}
