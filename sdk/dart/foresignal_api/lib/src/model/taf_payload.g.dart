// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taf_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TafPayloadCWProxy {
  TafPayload stationId(String stationId);

  TafPayload issueTime(DateTime issueTime);

  TafPayload validFrom(DateTime validFrom);

  TafPayload validTo(DateTime validTo);

  TafPayload forecast(List<TafPayloadForecastInner>? forecast);

  TafPayload rawText(String rawText);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TafPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TafPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  TafPayload call({
    String stationId,
    DateTime issueTime,
    DateTime validFrom,
    DateTime validTo,
    List<TafPayloadForecastInner>? forecast,
    String rawText,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTafPayload.copyWith(...)` or call `instanceOfTafPayload.copyWith.fieldName(value)` for a single field.
class _$TafPayloadCWProxyImpl implements _$TafPayloadCWProxy {
  const _$TafPayloadCWProxyImpl(this._value);

  final TafPayload _value;

  @override
  TafPayload stationId(String stationId) => call(stationId: stationId);

  @override
  TafPayload issueTime(DateTime issueTime) => call(issueTime: issueTime);

  @override
  TafPayload validFrom(DateTime validFrom) => call(validFrom: validFrom);

  @override
  TafPayload validTo(DateTime validTo) => call(validTo: validTo);

  @override
  TafPayload forecast(List<TafPayloadForecastInner>? forecast) =>
      call(forecast: forecast);

  @override
  TafPayload rawText(String rawText) => call(rawText: rawText);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TafPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TafPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TafPayload call({
    Object? stationId = const $CopyWithPlaceholder(),
    Object? issueTime = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validTo = const $CopyWithPlaceholder(),
    Object? forecast = const $CopyWithPlaceholder(),
    Object? rawText = const $CopyWithPlaceholder(),
  }) {
    return TafPayload(
      stationId: stationId == const $CopyWithPlaceholder() || stationId == null
          ? _value.stationId
          // ignore: cast_nullable_to_non_nullable
          : stationId as String,
      issueTime: issueTime == const $CopyWithPlaceholder() || issueTime == null
          ? _value.issueTime
          // ignore: cast_nullable_to_non_nullable
          : issueTime as DateTime,
      validFrom: validFrom == const $CopyWithPlaceholder() || validFrom == null
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime,
      validTo: validTo == const $CopyWithPlaceholder() || validTo == null
          ? _value.validTo
          // ignore: cast_nullable_to_non_nullable
          : validTo as DateTime,
      forecast: forecast == const $CopyWithPlaceholder()
          ? _value.forecast
          // ignore: cast_nullable_to_non_nullable
          : forecast as List<TafPayloadForecastInner>?,
      rawText: rawText == const $CopyWithPlaceholder() || rawText == null
          ? _value.rawText
          // ignore: cast_nullable_to_non_nullable
          : rawText as String,
    );
  }
}

extension $TafPayloadCopyWith on TafPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTafPayload.copyWith(...)` or `instanceOfTafPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TafPayloadCWProxy get copyWith => _$TafPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TafPayload _$TafPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TafPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'stationId',
      'issueTime',
      'validFrom',
      'validTo',
      'rawText',
    ],
  );
  final val = TafPayload(
    stationId: $checkedConvert('stationId', (v) => v as String),
    issueTime: $checkedConvert('issueTime', (v) => DateTime.parse(v as String)),
    validFrom: $checkedConvert('validFrom', (v) => DateTime.parse(v as String)),
    validTo: $checkedConvert('validTo', (v) => DateTime.parse(v as String)),
    forecast: $checkedConvert(
      'forecast',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => TafPayloadForecastInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    rawText: $checkedConvert('rawText', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$TafPayloadToJson(TafPayload instance) =>
    <String, dynamic>{
      'stationId': instance.stationId,
      'issueTime': instance.issueTime.toIso8601String(),
      'validFrom': instance.validFrom.toIso8601String(),
      'validTo': instance.validTo.toIso8601String(),
      'forecast': ?instance.forecast?.map((e) => e.toJson()).toList(),
      'rawText': instance.rawText,
    };
