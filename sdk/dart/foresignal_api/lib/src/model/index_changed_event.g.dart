// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'index_changed_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndexChangedEventCWProxy {
  IndexChangedEvent indexId(String indexId);

  IndexChangedEvent region(String region);

  IndexChangedEvent day(DateTime day);

  IndexChangedEvent value(num value);

  IndexChangedEvent previousValue(num? previousValue);

  IndexChangedEvent delta24h(num? delta24h);

  IndexChangedEvent delta7d(num? delta7d);

  IndexChangedEvent weightsOrigin(
    IndexChangedEventWeightsOriginEnum? weightsOrigin,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndexChangedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndexChangedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  IndexChangedEvent call({
    String indexId,
    String region,
    DateTime day,
    num value,
    num? previousValue,
    num? delta24h,
    num? delta7d,
    IndexChangedEventWeightsOriginEnum? weightsOrigin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndexChangedEvent.copyWith(...)` or call `instanceOfIndexChangedEvent.copyWith.fieldName(value)` for a single field.
class _$IndexChangedEventCWProxyImpl implements _$IndexChangedEventCWProxy {
  const _$IndexChangedEventCWProxyImpl(this._value);

  final IndexChangedEvent _value;

  @override
  IndexChangedEvent indexId(String indexId) => call(indexId: indexId);

  @override
  IndexChangedEvent region(String region) => call(region: region);

  @override
  IndexChangedEvent day(DateTime day) => call(day: day);

  @override
  IndexChangedEvent value(num value) => call(value: value);

  @override
  IndexChangedEvent previousValue(num? previousValue) =>
      call(previousValue: previousValue);

  @override
  IndexChangedEvent delta24h(num? delta24h) => call(delta24h: delta24h);

  @override
  IndexChangedEvent delta7d(num? delta7d) => call(delta7d: delta7d);

  @override
  IndexChangedEvent weightsOrigin(
    IndexChangedEventWeightsOriginEnum? weightsOrigin,
  ) => call(weightsOrigin: weightsOrigin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndexChangedEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndexChangedEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndexChangedEvent call({
    Object? indexId = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
    Object? day = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? previousValue = const $CopyWithPlaceholder(),
    Object? delta24h = const $CopyWithPlaceholder(),
    Object? delta7d = const $CopyWithPlaceholder(),
    Object? weightsOrigin = const $CopyWithPlaceholder(),
  }) {
    return IndexChangedEvent(
      indexId: indexId == const $CopyWithPlaceholder() || indexId == null
          ? _value.indexId
          // ignore: cast_nullable_to_non_nullable
          : indexId as String,
      region: region == const $CopyWithPlaceholder() || region == null
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as String,
      day: day == const $CopyWithPlaceholder() || day == null
          ? _value.day
          // ignore: cast_nullable_to_non_nullable
          : day as DateTime,
      value: value == const $CopyWithPlaceholder() || value == null
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num,
      previousValue: previousValue == const $CopyWithPlaceholder()
          ? _value.previousValue
          // ignore: cast_nullable_to_non_nullable
          : previousValue as num?,
      delta24h: delta24h == const $CopyWithPlaceholder()
          ? _value.delta24h
          // ignore: cast_nullable_to_non_nullable
          : delta24h as num?,
      delta7d: delta7d == const $CopyWithPlaceholder()
          ? _value.delta7d
          // ignore: cast_nullable_to_non_nullable
          : delta7d as num?,
      weightsOrigin: weightsOrigin == const $CopyWithPlaceholder()
          ? _value.weightsOrigin
          // ignore: cast_nullable_to_non_nullable
          : weightsOrigin as IndexChangedEventWeightsOriginEnum?,
    );
  }
}

extension $IndexChangedEventCopyWith on IndexChangedEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndexChangedEvent.copyWith(...)` or `instanceOfIndexChangedEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndexChangedEventCWProxy get copyWith =>
      _$IndexChangedEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndexChangedEvent _$IndexChangedEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IndexChangedEvent', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['indexId', 'region', 'day', 'value']);
  final val = IndexChangedEvent(
    indexId: $checkedConvert('indexId', (v) => v as String),
    region: $checkedConvert('region', (v) => v as String),
    day: $checkedConvert('day', (v) => DateTime.parse(v as String)),
    value: $checkedConvert('value', (v) => v as num),
    previousValue: $checkedConvert('previousValue', (v) => v as num?),
    delta24h: $checkedConvert('delta24h', (v) => v as num?),
    delta7d: $checkedConvert('delta7d', (v) => v as num?),
    weightsOrigin: $checkedConvert(
      'weightsOrigin',
      (v) =>
          $enumDecodeNullable(_$IndexChangedEventWeightsOriginEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$IndexChangedEventToJson(IndexChangedEvent instance) =>
    <String, dynamic>{
      'indexId': instance.indexId,
      'region': instance.region,
      'day': instance.day.toIso8601String(),
      'value': instance.value,
      'previousValue': ?instance.previousValue,
      'delta24h': ?instance.delta24h,
      'delta7d': ?instance.delta7d,
      'weightsOrigin':
          ?_$IndexChangedEventWeightsOriginEnumEnumMap[instance.weightsOrigin],
    };

const _$IndexChangedEventWeightsOriginEnumEnumMap = {
  IndexChangedEventWeightsOriginEnum.default_: 'default',
  IndexChangedEventWeightsOriginEnum.fitted: 'fitted',
  IndexChangedEventWeightsOriginEnum.operator_: 'operator',
};
