// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_stream_filter_external.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStreamFilterExternalCWProxy {
  EventStreamFilterExternal sourceIds(List<String>? sourceIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamFilterExternal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamFilterExternal(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStreamFilterExternal call({List<String>? sourceIds});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStreamFilterExternal.copyWith(...)` or call `instanceOfEventStreamFilterExternal.copyWith.fieldName(value)` for a single field.
class _$EventStreamFilterExternalCWProxyImpl
    implements _$EventStreamFilterExternalCWProxy {
  const _$EventStreamFilterExternalCWProxyImpl(this._value);

  final EventStreamFilterExternal _value;

  @override
  EventStreamFilterExternal sourceIds(List<String>? sourceIds) =>
      call(sourceIds: sourceIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamFilterExternal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamFilterExternal(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStreamFilterExternal call({
    Object? sourceIds = const $CopyWithPlaceholder(),
  }) {
    return EventStreamFilterExternal(
      sourceIds: sourceIds == const $CopyWithPlaceholder()
          ? _value.sourceIds
          // ignore: cast_nullable_to_non_nullable
          : sourceIds as List<String>?,
    );
  }
}

extension $EventStreamFilterExternalCopyWith on EventStreamFilterExternal {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStreamFilterExternal.copyWith(...)` or `instanceOfEventStreamFilterExternal.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStreamFilterExternalCWProxy get copyWith =>
      _$EventStreamFilterExternalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStreamFilterExternal _$EventStreamFilterExternalFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStreamFilterExternal', json, ($checkedConvert) {
  final val = EventStreamFilterExternal(
    sourceIds: $checkedConvert(
      'sourceIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$EventStreamFilterExternalToJson(
  EventStreamFilterExternal instance,
) => <String, dynamic>{'sourceIds': ?instance.sourceIds};
