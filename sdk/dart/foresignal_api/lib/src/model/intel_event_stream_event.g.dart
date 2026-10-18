// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_event_stream_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelEventStreamEventCWProxy {
  IntelEventStreamEvent eventId(String eventId);

  IntelEventStreamEvent change(IntelEventStreamEventChangeEnum change);

  IntelEventStreamEvent version(int version);

  IntelEventStreamEvent summary(IntelEventSummary summary);

  IntelEventStreamEvent mergedInto(String? mergedInto);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventStreamEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventStreamEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelEventStreamEvent call({
    String eventId,
    IntelEventStreamEventChangeEnum change,
    int version,
    IntelEventSummary summary,
    String? mergedInto,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelEventStreamEvent.copyWith(...)` or call `instanceOfIntelEventStreamEvent.copyWith.fieldName(value)` for a single field.
class _$IntelEventStreamEventCWProxyImpl
    implements _$IntelEventStreamEventCWProxy {
  const _$IntelEventStreamEventCWProxyImpl(this._value);

  final IntelEventStreamEvent _value;

  @override
  IntelEventStreamEvent eventId(String eventId) => call(eventId: eventId);

  @override
  IntelEventStreamEvent change(IntelEventStreamEventChangeEnum change) =>
      call(change: change);

  @override
  IntelEventStreamEvent version(int version) => call(version: version);

  @override
  IntelEventStreamEvent summary(IntelEventSummary summary) =>
      call(summary: summary);

  @override
  IntelEventStreamEvent mergedInto(String? mergedInto) =>
      call(mergedInto: mergedInto);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelEventStreamEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelEventStreamEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelEventStreamEvent call({
    Object? eventId = const $CopyWithPlaceholder(),
    Object? change = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
  }) {
    return IntelEventStreamEvent(
      eventId: eventId == const $CopyWithPlaceholder() || eventId == null
          ? _value.eventId
          // ignore: cast_nullable_to_non_nullable
          : eventId as String,
      change: change == const $CopyWithPlaceholder() || change == null
          ? _value.change
          // ignore: cast_nullable_to_non_nullable
          : change as IntelEventStreamEventChangeEnum,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      summary: summary == const $CopyWithPlaceholder() || summary == null
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as IntelEventSummary,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
    );
  }
}

extension $IntelEventStreamEventCopyWith on IntelEventStreamEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelEventStreamEvent.copyWith(...)` or `instanceOfIntelEventStreamEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelEventStreamEventCWProxy get copyWith =>
      _$IntelEventStreamEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelEventStreamEvent _$IntelEventStreamEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelEventStreamEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['eventId', 'change', 'version', 'summary'],
  );
  final val = IntelEventStreamEvent(
    eventId: $checkedConvert('eventId', (v) => v as String),
    change: $checkedConvert(
      'change',
      (v) => $enumDecode(_$IntelEventStreamEventChangeEnumEnumMap, v),
    ),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    summary: $checkedConvert(
      'summary',
      (v) => IntelEventSummary.fromJson(v as Map<String, dynamic>),
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$IntelEventStreamEventToJson(
  IntelEventStreamEvent instance,
) => <String, dynamic>{
  'eventId': instance.eventId,
  'change': _$IntelEventStreamEventChangeEnumEnumMap[instance.change]!,
  'version': instance.version,
  'summary': instance.summary.toJson(),
  'mergedInto': ?instance.mergedInto,
};

const _$IntelEventStreamEventChangeEnumEnumMap = {
  IntelEventStreamEventChangeEnum.created: 'created',
  IntelEventStreamEventChangeEnum.updated: 'updated',
  IntelEventStreamEventChangeEnum.resolved: 'resolved',
  IntelEventStreamEventChangeEnum.merged: 'merged',
};
