// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'aggregator_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AggregatorConfigCWProxy {
  AggregatorConfig enabled(bool enabled);

  AggregatorConfig mode(String? mode);

  AggregatorConfig endpoint(String? endpoint);

  AggregatorConfig config(Map<String, Object>? config);

  AggregatorConfig disableSupersededSources(bool? disableSupersededSources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AggregatorConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AggregatorConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  AggregatorConfig call({
    bool enabled,
    String? mode,
    String? endpoint,
    Map<String, Object>? config,
    bool? disableSupersededSources,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAggregatorConfig.copyWith(...)` or call `instanceOfAggregatorConfig.copyWith.fieldName(value)` for a single field.
class _$AggregatorConfigCWProxyImpl implements _$AggregatorConfigCWProxy {
  const _$AggregatorConfigCWProxyImpl(this._value);

  final AggregatorConfig _value;

  @override
  AggregatorConfig enabled(bool enabled) => call(enabled: enabled);

  @override
  AggregatorConfig mode(String? mode) => call(mode: mode);

  @override
  AggregatorConfig endpoint(String? endpoint) => call(endpoint: endpoint);

  @override
  AggregatorConfig config(Map<String, Object>? config) => call(config: config);

  @override
  AggregatorConfig disableSupersededSources(bool? disableSupersededSources) =>
      call(disableSupersededSources: disableSupersededSources);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AggregatorConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AggregatorConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AggregatorConfig call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? endpoint = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? disableSupersededSources = const $CopyWithPlaceholder(),
  }) {
    return AggregatorConfig(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      mode: mode == const $CopyWithPlaceholder()
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as String?,
      endpoint: endpoint == const $CopyWithPlaceholder()
          ? _value.endpoint
          // ignore: cast_nullable_to_non_nullable
          : endpoint as String?,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>?,
      disableSupersededSources:
          disableSupersededSources == const $CopyWithPlaceholder()
          ? _value.disableSupersededSources
          // ignore: cast_nullable_to_non_nullable
          : disableSupersededSources as bool?,
    );
  }
}

extension $AggregatorConfigCopyWith on AggregatorConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAggregatorConfig.copyWith(...)` or `instanceOfAggregatorConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AggregatorConfigCWProxy get copyWith => _$AggregatorConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AggregatorConfig _$AggregatorConfigFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AggregatorConfig', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['enabled']);
      final val = AggregatorConfig(
        enabled: $checkedConvert('enabled', (v) => v as bool),
        mode: $checkedConvert('mode', (v) => v as String?),
        endpoint: $checkedConvert('endpoint', (v) => v as String?),
        config: $checkedConvert(
          'config',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        disableSupersededSources: $checkedConvert(
          'disableSupersededSources',
          (v) => v as bool? ?? true,
        ),
      );
      return val;
    });

Map<String, dynamic> _$AggregatorConfigToJson(AggregatorConfig instance) =>
    <String, dynamic>{
      'enabled': instance.enabled,
      'mode': ?instance.mode,
      'endpoint': ?instance.endpoint,
      'config': ?instance.config,
      'disableSupersededSources': ?instance.disableSupersededSources,
    };
