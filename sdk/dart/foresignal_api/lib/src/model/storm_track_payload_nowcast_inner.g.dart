// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'storm_track_payload_nowcast_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StormTrackPayloadNowcastInnerCWProxy {
  StormTrackPayloadNowcastInner leadMinutes(
    StormTrackPayloadNowcastInnerLeadMinutesEnum leadMinutes,
  );

  StormTrackPayloadNowcastInner method(
    StormTrackPayloadNowcastInnerMethodEnum method,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadNowcastInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadNowcastInner(...).copyWith(id: 12, name: "My name")
  /// ```
  StormTrackPayloadNowcastInner call({
    StormTrackPayloadNowcastInnerLeadMinutesEnum leadMinutes,
    StormTrackPayloadNowcastInnerMethodEnum method,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStormTrackPayloadNowcastInner.copyWith(...)` or call `instanceOfStormTrackPayloadNowcastInner.copyWith.fieldName(value)` for a single field.
class _$StormTrackPayloadNowcastInnerCWProxyImpl
    implements _$StormTrackPayloadNowcastInnerCWProxy {
  const _$StormTrackPayloadNowcastInnerCWProxyImpl(this._value);

  final StormTrackPayloadNowcastInner _value;

  @override
  StormTrackPayloadNowcastInner leadMinutes(
    StormTrackPayloadNowcastInnerLeadMinutesEnum leadMinutes,
  ) => call(leadMinutes: leadMinutes);

  @override
  StormTrackPayloadNowcastInner method(
    StormTrackPayloadNowcastInnerMethodEnum method,
  ) => call(method: method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadNowcastInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadNowcastInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StormTrackPayloadNowcastInner call({
    Object? leadMinutes = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
  }) {
    return StormTrackPayloadNowcastInner(
      leadMinutes:
          leadMinutes == const $CopyWithPlaceholder() || leadMinutes == null
          ? _value.leadMinutes
          // ignore: cast_nullable_to_non_nullable
          : leadMinutes as StormTrackPayloadNowcastInnerLeadMinutesEnum,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as StormTrackPayloadNowcastInnerMethodEnum,
    );
  }
}

extension $StormTrackPayloadNowcastInnerCopyWith
    on StormTrackPayloadNowcastInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStormTrackPayloadNowcastInner.copyWith(...)` or `instanceOfStormTrackPayloadNowcastInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StormTrackPayloadNowcastInnerCWProxy get copyWith =>
      _$StormTrackPayloadNowcastInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StormTrackPayloadNowcastInner _$StormTrackPayloadNowcastInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StormTrackPayloadNowcastInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['leadMinutes', 'method']);
  final val = StormTrackPayloadNowcastInner(
    leadMinutes: $checkedConvert(
      'leadMinutes',
      (v) =>
          $enumDecode(_$StormTrackPayloadNowcastInnerLeadMinutesEnumEnumMap, v),
    ),
    method: $checkedConvert(
      'method',
      (v) => $enumDecode(_$StormTrackPayloadNowcastInnerMethodEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$StormTrackPayloadNowcastInnerToJson(
  StormTrackPayloadNowcastInner instance,
) => <String, dynamic>{
  'leadMinutes':
      _$StormTrackPayloadNowcastInnerLeadMinutesEnumEnumMap[instance
          .leadMinutes]!,
  'method': _$StormTrackPayloadNowcastInnerMethodEnumEnumMap[instance.method]!,
};

const _$StormTrackPayloadNowcastInnerLeadMinutesEnumEnumMap = {
  StormTrackPayloadNowcastInnerLeadMinutesEnum.number15: 15,
  StormTrackPayloadNowcastInnerLeadMinutesEnum.number30: 30,
  StormTrackPayloadNowcastInnerLeadMinutesEnum.number60: 60,
};

const _$StormTrackPayloadNowcastInnerMethodEnumEnumMap = {
  StormTrackPayloadNowcastInnerMethodEnum.linearAdvection: 'linear-advection',
};
