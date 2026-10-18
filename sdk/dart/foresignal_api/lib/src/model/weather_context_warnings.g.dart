// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_context_warnings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherContextWarningsCWProxy {
  WeatherContextWarnings status(WeatherSectionStatus status);

  WeatherContextWarnings asOf(DateTime? asOf);

  WeatherContextWarnings statusText(String? statusText);

  WeatherContextWarnings items(List<WeatherContextWarningsItemsInner>? items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextWarnings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextWarnings(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherContextWarnings call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    List<WeatherContextWarningsItemsInner>? items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherContextWarnings.copyWith(...)` or call `instanceOfWeatherContextWarnings.copyWith.fieldName(value)` for a single field.
class _$WeatherContextWarningsCWProxyImpl
    implements _$WeatherContextWarningsCWProxy {
  const _$WeatherContextWarningsCWProxyImpl(this._value);

  final WeatherContextWarnings _value;

  @override
  WeatherContextWarnings status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  WeatherContextWarnings asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  WeatherContextWarnings statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  WeatherContextWarnings items(List<WeatherContextWarningsItemsInner>? items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextWarnings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextWarnings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherContextWarnings call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return WeatherContextWarnings(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as WeatherSectionStatus,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      statusText: statusText == const $CopyWithPlaceholder()
          ? _value.statusText
          // ignore: cast_nullable_to_non_nullable
          : statusText as String?,
      items: items == const $CopyWithPlaceholder()
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<WeatherContextWarningsItemsInner>?,
    );
  }
}

extension $WeatherContextWarningsCopyWith on WeatherContextWarnings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherContextWarnings.copyWith(...)` or `instanceOfWeatherContextWarnings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherContextWarningsCWProxy get copyWith =>
      _$WeatherContextWarningsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherContextWarnings _$WeatherContextWarningsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WeatherContextWarnings', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = WeatherContextWarnings(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    statusText: $checkedConvert('statusText', (v) => v as String?),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => WeatherContextWarningsItemsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$WeatherContextWarningsToJson(
  WeatherContextWarnings instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'items': ?instance.items?.map((e) => e.toJson()).toList(),
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
