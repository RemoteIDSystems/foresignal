// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_stream_filter.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStreamFilterCWProxy {
  EventStreamFilter topics(List<String>? topics);

  EventStreamFilter changes(EventStreamChangesFilter? changes);

  EventStreamFilter alerts(EventStreamFilterAlerts? alerts);

  EventStreamFilter notifications(Map<String, Object>? notifications);

  EventStreamFilter workspace(EventStreamFilterWorkspace? workspace);

  EventStreamFilter chronicle(EventStreamFilterChronicle? chronicle);

  EventStreamFilter external_(EventStreamFilterExternal? external_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamFilter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamFilter(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStreamFilter call({
    List<String>? topics,
    EventStreamChangesFilter? changes,
    EventStreamFilterAlerts? alerts,
    Map<String, Object>? notifications,
    EventStreamFilterWorkspace? workspace,
    EventStreamFilterChronicle? chronicle,
    EventStreamFilterExternal? external_,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStreamFilter.copyWith(...)` or call `instanceOfEventStreamFilter.copyWith.fieldName(value)` for a single field.
class _$EventStreamFilterCWProxyImpl implements _$EventStreamFilterCWProxy {
  const _$EventStreamFilterCWProxyImpl(this._value);

  final EventStreamFilter _value;

  @override
  EventStreamFilter topics(List<String>? topics) => call(topics: topics);

  @override
  EventStreamFilter changes(EventStreamChangesFilter? changes) =>
      call(changes: changes);

  @override
  EventStreamFilter alerts(EventStreamFilterAlerts? alerts) =>
      call(alerts: alerts);

  @override
  EventStreamFilter notifications(Map<String, Object>? notifications) =>
      call(notifications: notifications);

  @override
  EventStreamFilter workspace(EventStreamFilterWorkspace? workspace) =>
      call(workspace: workspace);

  @override
  EventStreamFilter chronicle(EventStreamFilterChronicle? chronicle) =>
      call(chronicle: chronicle);

  @override
  EventStreamFilter external_(EventStreamFilterExternal? external_) =>
      call(external_: external_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamFilter(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamFilter(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStreamFilter call({
    Object? topics = const $CopyWithPlaceholder(),
    Object? changes = const $CopyWithPlaceholder(),
    Object? alerts = const $CopyWithPlaceholder(),
    Object? notifications = const $CopyWithPlaceholder(),
    Object? workspace = const $CopyWithPlaceholder(),
    Object? chronicle = const $CopyWithPlaceholder(),
    Object? external_ = const $CopyWithPlaceholder(),
  }) {
    return EventStreamFilter(
      topics: topics == const $CopyWithPlaceholder()
          ? _value.topics
          // ignore: cast_nullable_to_non_nullable
          : topics as List<String>?,
      changes: changes == const $CopyWithPlaceholder()
          ? _value.changes
          // ignore: cast_nullable_to_non_nullable
          : changes as EventStreamChangesFilter?,
      alerts: alerts == const $CopyWithPlaceholder()
          ? _value.alerts
          // ignore: cast_nullable_to_non_nullable
          : alerts as EventStreamFilterAlerts?,
      notifications: notifications == const $CopyWithPlaceholder()
          ? _value.notifications
          // ignore: cast_nullable_to_non_nullable
          : notifications as Map<String, Object>?,
      workspace: workspace == const $CopyWithPlaceholder()
          ? _value.workspace
          // ignore: cast_nullable_to_non_nullable
          : workspace as EventStreamFilterWorkspace?,
      chronicle: chronicle == const $CopyWithPlaceholder()
          ? _value.chronicle
          // ignore: cast_nullable_to_non_nullable
          : chronicle as EventStreamFilterChronicle?,
      external_: external_ == const $CopyWithPlaceholder()
          ? _value.external_
          // ignore: cast_nullable_to_non_nullable
          : external_ as EventStreamFilterExternal?,
    );
  }
}

extension $EventStreamFilterCopyWith on EventStreamFilter {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStreamFilter.copyWith(...)` or `instanceOfEventStreamFilter.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStreamFilterCWProxy get copyWith =>
      _$EventStreamFilterCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStreamFilter _$EventStreamFilterFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EventStreamFilter', json, ($checkedConvert) {
      final val = EventStreamFilter(
        topics: $checkedConvert(
          'topics',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        changes: $checkedConvert(
          'changes',
          (v) => v == null
              ? null
              : EventStreamChangesFilter.fromJson(v as Map<String, dynamic>),
        ),
        alerts: $checkedConvert(
          'alerts',
          (v) => v == null
              ? null
              : EventStreamFilterAlerts.fromJson(v as Map<String, dynamic>),
        ),
        notifications: $checkedConvert(
          'notifications',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        workspace: $checkedConvert(
          'workspace',
          (v) => v == null
              ? null
              : EventStreamFilterWorkspace.fromJson(v as Map<String, dynamic>),
        ),
        chronicle: $checkedConvert(
          'chronicle',
          (v) => v == null
              ? null
              : EventStreamFilterChronicle.fromJson(v as Map<String, dynamic>),
        ),
        external_: $checkedConvert(
          'external',
          (v) => v == null
              ? null
              : EventStreamFilterExternal.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    }, fieldKeyMap: const {'external_': 'external'});

Map<String, dynamic> _$EventStreamFilterToJson(EventStreamFilter instance) =>
    <String, dynamic>{
      'topics': ?instance.topics,
      'changes': ?instance.changes?.toJson(),
      'alerts': ?instance.alerts?.toJson(),
      'notifications': ?instance.notifications,
      'workspace': ?instance.workspace?.toJson(),
      'chronicle': ?instance.chronicle?.toJson(),
      'external': ?instance.external_?.toJson(),
    };
