// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'plugin_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PluginSettingsCWProxy {
  PluginSettings enabled(bool enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PluginSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PluginSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  PluginSettings call({bool enabled});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPluginSettings.copyWith(...)` or call `instanceOfPluginSettings.copyWith.fieldName(value)` for a single field.
class _$PluginSettingsCWProxyImpl implements _$PluginSettingsCWProxy {
  const _$PluginSettingsCWProxyImpl(this._value);

  final PluginSettings _value;

  @override
  PluginSettings enabled(bool enabled) => call(enabled: enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PluginSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PluginSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PluginSettings call({Object? enabled = const $CopyWithPlaceholder()}) {
    return PluginSettings(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
    );
  }
}

extension $PluginSettingsCopyWith on PluginSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPluginSettings.copyWith(...)` or `instanceOfPluginSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PluginSettingsCWProxy get copyWith => _$PluginSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PluginSettings _$PluginSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PluginSettings', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['enabled']);
      final val = PluginSettings(
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$PluginSettingsToJson(PluginSettings instance) =>
    <String, dynamic>{'enabled': instance.enabled};
