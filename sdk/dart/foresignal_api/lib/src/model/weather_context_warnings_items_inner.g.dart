// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_context_warnings_items_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherContextWarningsItemsInnerCWProxy {
  WeatherContextWarningsItemsInner id(String id);

  WeatherContextWarningsItemsInner event(String event);

  WeatherContextWarningsItemsInner severity(String? severity);

  WeatherContextWarningsItemsInner expires(DateTime? expires);

  WeatherContextWarningsItemsInner untrusted(bool? untrusted);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextWarningsItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextWarningsItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherContextWarningsItemsInner call({
    String id,
    String event,
    String? severity,
    DateTime? expires,
    bool? untrusted,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherContextWarningsItemsInner.copyWith(...)` or call `instanceOfWeatherContextWarningsItemsInner.copyWith.fieldName(value)` for a single field.
class _$WeatherContextWarningsItemsInnerCWProxyImpl
    implements _$WeatherContextWarningsItemsInnerCWProxy {
  const _$WeatherContextWarningsItemsInnerCWProxyImpl(this._value);

  final WeatherContextWarningsItemsInner _value;

  @override
  WeatherContextWarningsItemsInner id(String id) => call(id: id);

  @override
  WeatherContextWarningsItemsInner event(String event) => call(event: event);

  @override
  WeatherContextWarningsItemsInner severity(String? severity) =>
      call(severity: severity);

  @override
  WeatherContextWarningsItemsInner expires(DateTime? expires) =>
      call(expires: expires);

  @override
  WeatherContextWarningsItemsInner untrusted(bool? untrusted) =>
      call(untrusted: untrusted);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextWarningsItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextWarningsItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherContextWarningsItemsInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? expires = const $CopyWithPlaceholder(),
    Object? untrusted = const $CopyWithPlaceholder(),
  }) {
    return WeatherContextWarningsItemsInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as String,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as String?,
      expires: expires == const $CopyWithPlaceholder()
          ? _value.expires
          // ignore: cast_nullable_to_non_nullable
          : expires as DateTime?,
      untrusted: untrusted == const $CopyWithPlaceholder()
          ? _value.untrusted
          // ignore: cast_nullable_to_non_nullable
          : untrusted as bool?,
    );
  }
}

extension $WeatherContextWarningsItemsInnerCopyWith
    on WeatherContextWarningsItemsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherContextWarningsItemsInner.copyWith(...)` or `instanceOfWeatherContextWarningsItemsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherContextWarningsItemsInnerCWProxy get copyWith =>
      _$WeatherContextWarningsItemsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherContextWarningsItemsInner _$WeatherContextWarningsItemsInnerFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('WeatherContextWarningsItemsInner', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'event']);
      final val = WeatherContextWarningsItemsInner(
        id: $checkedConvert('id', (v) => v as String),
        event: $checkedConvert('event', (v) => v as String),
        severity: $checkedConvert('severity', (v) => v as String?),
        expires: $checkedConvert(
          'expires',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        untrusted: $checkedConvert('untrusted', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$WeatherContextWarningsItemsInnerToJson(
  WeatherContextWarningsItemsInner instance,
) => <String, dynamic>{
  'id': instance.id,
  'event': instance.event,
  'severity': ?instance.severity,
  'expires': ?instance.expires?.toIso8601String(),
  'untrusted': ?instance.untrusted,
};
