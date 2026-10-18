// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'plugin_list.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PluginListCWProxy {
  PluginList enabled(bool enabled);

  PluginList plugins(List<Plugin> plugins);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PluginList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PluginList(...).copyWith(id: 12, name: "My name")
  /// ```
  PluginList call({bool enabled, List<Plugin> plugins});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPluginList.copyWith(...)` or call `instanceOfPluginList.copyWith.fieldName(value)` for a single field.
class _$PluginListCWProxyImpl implements _$PluginListCWProxy {
  const _$PluginListCWProxyImpl(this._value);

  final PluginList _value;

  @override
  PluginList enabled(bool enabled) => call(enabled: enabled);

  @override
  PluginList plugins(List<Plugin> plugins) => call(plugins: plugins);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PluginList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PluginList(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PluginList call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? plugins = const $CopyWithPlaceholder(),
  }) {
    return PluginList(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      plugins: plugins == const $CopyWithPlaceholder() || plugins == null
          ? _value.plugins
          // ignore: cast_nullable_to_non_nullable
          : plugins as List<Plugin>,
    );
  }
}

extension $PluginListCopyWith on PluginList {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPluginList.copyWith(...)` or `instanceOfPluginList.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PluginListCWProxy get copyWith => _$PluginListCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PluginList _$PluginListFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PluginList', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['enabled', 'plugins']);
      final val = PluginList(
        enabled: $checkedConvert('enabled', (v) => v as bool),
        plugins: $checkedConvert(
          'plugins',
          (v) => (v as List<dynamic>)
              .map((e) => Plugin.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PluginListToJson(PluginList instance) =>
    <String, dynamic>{
      'enabled': instance.enabled,
      'plugins': instance.plugins.map((e) => e.toJson()).toList(),
    };
