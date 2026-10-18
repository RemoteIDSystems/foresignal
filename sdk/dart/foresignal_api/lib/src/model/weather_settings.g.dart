// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherSettingsCWProxy {
  WeatherSettings alertContext(WeatherSettingsAlertContextEnum? alertContext);

  WeatherSettings defaultAoi(String? defaultAoi);

  WeatherSettings aoiBufferKm(num? aoiBufferKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherSettings call({
    WeatherSettingsAlertContextEnum? alertContext,
    String? defaultAoi,
    num? aoiBufferKm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherSettings.copyWith(...)` or call `instanceOfWeatherSettings.copyWith.fieldName(value)` for a single field.
class _$WeatherSettingsCWProxyImpl implements _$WeatherSettingsCWProxy {
  const _$WeatherSettingsCWProxyImpl(this._value);

  final WeatherSettings _value;

  @override
  WeatherSettings alertContext(WeatherSettingsAlertContextEnum? alertContext) =>
      call(alertContext: alertContext);

  @override
  WeatherSettings defaultAoi(String? defaultAoi) =>
      call(defaultAoi: defaultAoi);

  @override
  WeatherSettings aoiBufferKm(num? aoiBufferKm) =>
      call(aoiBufferKm: aoiBufferKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherSettings call({
    Object? alertContext = const $CopyWithPlaceholder(),
    Object? defaultAoi = const $CopyWithPlaceholder(),
    Object? aoiBufferKm = const $CopyWithPlaceholder(),
  }) {
    return WeatherSettings(
      alertContext: alertContext == const $CopyWithPlaceholder()
          ? _value.alertContext
          // ignore: cast_nullable_to_non_nullable
          : alertContext as WeatherSettingsAlertContextEnum?,
      defaultAoi: defaultAoi == const $CopyWithPlaceholder()
          ? _value.defaultAoi
          // ignore: cast_nullable_to_non_nullable
          : defaultAoi as String?,
      aoiBufferKm: aoiBufferKm == const $CopyWithPlaceholder()
          ? _value.aoiBufferKm
          // ignore: cast_nullable_to_non_nullable
          : aoiBufferKm as num?,
    );
  }
}

extension $WeatherSettingsCopyWith on WeatherSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherSettings.copyWith(...)` or `instanceOfWeatherSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherSettingsCWProxy get copyWith => _$WeatherSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherSettings _$WeatherSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WeatherSettings', json, ($checkedConvert) {
      final val = WeatherSettings(
        alertContext: $checkedConvert(
          'alertContext',
          (v) =>
              $enumDecodeNullable(_$WeatherSettingsAlertContextEnumEnumMap, v),
        ),
        defaultAoi: $checkedConvert(
          'defaultAoi',
          (v) => v as String? ?? '-106.65,25.84,-93.51,36.50',
        ),
        aoiBufferKm: $checkedConvert('aoiBufferKm', (v) => v as num? ?? 150),
      );
      return val;
    });

Map<String, dynamic> _$WeatherSettingsToJson(WeatherSettings instance) =>
    <String, dynamic>{
      'alertContext':
          ?_$WeatherSettingsAlertContextEnumEnumMap[instance.alertContext],
      'defaultAoi': ?instance.defaultAoi,
      'aoiBufferKm': ?instance.aoiBufferKm,
    };

const _$WeatherSettingsAlertContextEnumEnumMap = {
  WeatherSettingsAlertContextEnum.auto: 'auto',
  WeatherSettingsAlertContextEnum.off: 'off',
};
