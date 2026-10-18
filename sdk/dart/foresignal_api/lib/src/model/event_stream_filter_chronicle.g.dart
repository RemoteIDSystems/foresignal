// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_stream_filter_chronicle.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStreamFilterChronicleCWProxy {
  EventStreamFilterChronicle chronicleIds(List<String>? chronicleIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamFilterChronicle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamFilterChronicle(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStreamFilterChronicle call({List<String>? chronicleIds});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStreamFilterChronicle.copyWith(...)` or call `instanceOfEventStreamFilterChronicle.copyWith.fieldName(value)` for a single field.
class _$EventStreamFilterChronicleCWProxyImpl
    implements _$EventStreamFilterChronicleCWProxy {
  const _$EventStreamFilterChronicleCWProxyImpl(this._value);

  final EventStreamFilterChronicle _value;

  @override
  EventStreamFilterChronicle chronicleIds(List<String>? chronicleIds) =>
      call(chronicleIds: chronicleIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStreamFilterChronicle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStreamFilterChronicle(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStreamFilterChronicle call({
    Object? chronicleIds = const $CopyWithPlaceholder(),
  }) {
    return EventStreamFilterChronicle(
      chronicleIds: chronicleIds == const $CopyWithPlaceholder()
          ? _value.chronicleIds
          // ignore: cast_nullable_to_non_nullable
          : chronicleIds as List<String>?,
    );
  }
}

extension $EventStreamFilterChronicleCopyWith on EventStreamFilterChronicle {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStreamFilterChronicle.copyWith(...)` or `instanceOfEventStreamFilterChronicle.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStreamFilterChronicleCWProxy get copyWith =>
      _$EventStreamFilterChronicleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStreamFilterChronicle _$EventStreamFilterChronicleFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventStreamFilterChronicle', json, ($checkedConvert) {
  final val = EventStreamFilterChronicle(
    chronicleIds: $checkedConvert(
      'chronicleIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$EventStreamFilterChronicleToJson(
  EventStreamFilterChronicle instance,
) => <String, dynamic>{'chronicleIds': ?instance.chronicleIds};
