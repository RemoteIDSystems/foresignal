// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attention_gap_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttentionGapEventCWProxy {
  AttentionGapEvent event(AttentionGapEventEventEnum event);

  AttentionGapEvent findingId(String findingId);

  AttentionGapEvent ref(String ref);

  AttentionGapEvent regionCodes(List<String>? regionCodes);

  AttentionGapEvent expected(num expected);

  AttentionGapEvent observed(num observed);

  AttentionGapEvent platforms(List<String> platforms);

  AttentionGapEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionGapEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionGapEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  AttentionGapEvent call({
    AttentionGapEventEventEnum event,
    String findingId,
    String ref,
    List<String>? regionCodes,
    num expected,
    num observed,
    List<String> platforms,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttentionGapEvent.copyWith(...)` or call `instanceOfAttentionGapEvent.copyWith.fieldName(value)` for a single field.
class _$AttentionGapEventCWProxyImpl implements _$AttentionGapEventCWProxy {
  const _$AttentionGapEventCWProxyImpl(this._value);

  final AttentionGapEvent _value;

  @override
  AttentionGapEvent event(AttentionGapEventEventEnum event) =>
      call(event: event);

  @override
  AttentionGapEvent findingId(String findingId) => call(findingId: findingId);

  @override
  AttentionGapEvent ref(String ref) => call(ref: ref);

  @override
  AttentionGapEvent regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  AttentionGapEvent expected(num expected) => call(expected: expected);

  @override
  AttentionGapEvent observed(num observed) => call(observed: observed);

  @override
  AttentionGapEvent platforms(List<String> platforms) =>
      call(platforms: platforms);

  @override
  AttentionGapEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionGapEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionGapEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AttentionGapEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? findingId = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? expected = const $CopyWithPlaceholder(),
    Object? observed = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return AttentionGapEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as AttentionGapEventEventEnum,
      findingId: findingId == const $CopyWithPlaceholder() || findingId == null
          ? _value.findingId
          // ignore: cast_nullable_to_non_nullable
          : findingId as String,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      expected: expected == const $CopyWithPlaceholder() || expected == null
          ? _value.expected
          // ignore: cast_nullable_to_non_nullable
          : expected as num,
      observed: observed == const $CopyWithPlaceholder() || observed == null
          ? _value.observed
          // ignore: cast_nullable_to_non_nullable
          : observed as num,
      platforms: platforms == const $CopyWithPlaceholder() || platforms == null
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as List<String>,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $AttentionGapEventCopyWith on AttentionGapEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttentionGapEvent.copyWith(...)` or `instanceOfAttentionGapEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttentionGapEventCWProxy get copyWith =>
      _$AttentionGapEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AttentionGapEvent _$AttentionGapEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AttentionGapEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'event',
          'findingId',
          'ref',
          'expected',
          'observed',
          'platforms',
          'at',
        ],
      );
      final val = AttentionGapEvent(
        event: $checkedConvert(
          'event',
          (v) => $enumDecode(_$AttentionGapEventEventEnumEnumMap, v),
        ),
        findingId: $checkedConvert('findingId', (v) => v as String),
        ref: $checkedConvert('ref', (v) => v as String),
        regionCodes: $checkedConvert(
          'regionCodes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        expected: $checkedConvert('expected', (v) => v as num),
        observed: $checkedConvert('observed', (v) => v as num),
        platforms: $checkedConvert(
          'platforms',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$AttentionGapEventToJson(AttentionGapEvent instance) =>
    <String, dynamic>{
      'event': _$AttentionGapEventEventEnumEnumMap[instance.event]!,
      'findingId': instance.findingId,
      'ref': instance.ref,
      'regionCodes': ?instance.regionCodes,
      'expected': instance.expected,
      'observed': instance.observed,
      'platforms': instance.platforms,
      'at': instance.at.toIso8601String(),
    };

const _$AttentionGapEventEventEnumEnumMap = {
  AttentionGapEventEventEnum.attentionPeriodGap: 'attention.gap',
};
