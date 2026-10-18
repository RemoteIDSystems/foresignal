// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'storm_track_payload_trend.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StormTrackPayloadTrendCWProxy {
  StormTrackPayloadTrend maxDbz(StormTrackPayloadTrendMaxDbzEnum? maxDbz);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadTrend(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadTrend(...).copyWith(id: 12, name: "My name")
  /// ```
  StormTrackPayloadTrend call({StormTrackPayloadTrendMaxDbzEnum? maxDbz});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStormTrackPayloadTrend.copyWith(...)` or call `instanceOfStormTrackPayloadTrend.copyWith.fieldName(value)` for a single field.
class _$StormTrackPayloadTrendCWProxyImpl
    implements _$StormTrackPayloadTrendCWProxy {
  const _$StormTrackPayloadTrendCWProxyImpl(this._value);

  final StormTrackPayloadTrend _value;

  @override
  StormTrackPayloadTrend maxDbz(StormTrackPayloadTrendMaxDbzEnum? maxDbz) =>
      call(maxDbz: maxDbz);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadTrend(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadTrend(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StormTrackPayloadTrend call({Object? maxDbz = const $CopyWithPlaceholder()}) {
    return StormTrackPayloadTrend(
      maxDbz: maxDbz == const $CopyWithPlaceholder()
          ? _value.maxDbz
          // ignore: cast_nullable_to_non_nullable
          : maxDbz as StormTrackPayloadTrendMaxDbzEnum?,
    );
  }
}

extension $StormTrackPayloadTrendCopyWith on StormTrackPayloadTrend {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStormTrackPayloadTrend.copyWith(...)` or `instanceOfStormTrackPayloadTrend.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StormTrackPayloadTrendCWProxy get copyWith =>
      _$StormTrackPayloadTrendCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StormTrackPayloadTrend _$StormTrackPayloadTrendFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StormTrackPayloadTrend', json, ($checkedConvert) {
  final val = StormTrackPayloadTrend(
    maxDbz: $checkedConvert(
      'maxDbz',
      (v) => $enumDecodeNullable(_$StormTrackPayloadTrendMaxDbzEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$StormTrackPayloadTrendToJson(
  StormTrackPayloadTrend instance,
) => <String, dynamic>{
  'maxDbz': ?_$StormTrackPayloadTrendMaxDbzEnumEnumMap[instance.maxDbz],
};

const _$StormTrackPayloadTrendMaxDbzEnumEnumMap = {
  StormTrackPayloadTrendMaxDbzEnum.rising: 'rising',
  StormTrackPayloadTrendMaxDbzEnum.steady: 'steady',
  StormTrackPayloadTrendMaxDbzEnum.falling: 'falling',
};
