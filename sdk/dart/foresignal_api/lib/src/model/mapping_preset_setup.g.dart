// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_preset_setup.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingPresetSetupCWProxy {
  MappingPresetSetup instructions(String instructions);

  MappingPresetSetup urlTemplate(String urlTemplate);

  MappingPresetSetup url(String? url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingPresetSetup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingPresetSetup(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingPresetSetup call({
    String instructions,
    String urlTemplate,
    String? url,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingPresetSetup.copyWith(...)` or call `instanceOfMappingPresetSetup.copyWith.fieldName(value)` for a single field.
class _$MappingPresetSetupCWProxyImpl implements _$MappingPresetSetupCWProxy {
  const _$MappingPresetSetupCWProxyImpl(this._value);

  final MappingPresetSetup _value;

  @override
  MappingPresetSetup instructions(String instructions) =>
      call(instructions: instructions);

  @override
  MappingPresetSetup urlTemplate(String urlTemplate) =>
      call(urlTemplate: urlTemplate);

  @override
  MappingPresetSetup url(String? url) => call(url: url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingPresetSetup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingPresetSetup(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingPresetSetup call({
    Object? instructions = const $CopyWithPlaceholder(),
    Object? urlTemplate = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
  }) {
    return MappingPresetSetup(
      instructions:
          instructions == const $CopyWithPlaceholder() || instructions == null
          ? _value.instructions
          // ignore: cast_nullable_to_non_nullable
          : instructions as String,
      urlTemplate:
          urlTemplate == const $CopyWithPlaceholder() || urlTemplate == null
          ? _value.urlTemplate
          // ignore: cast_nullable_to_non_nullable
          : urlTemplate as String,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
    );
  }
}

extension $MappingPresetSetupCopyWith on MappingPresetSetup {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingPresetSetup.copyWith(...)` or `instanceOfMappingPresetSetup.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingPresetSetupCWProxy get copyWith =>
      _$MappingPresetSetupCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingPresetSetup _$MappingPresetSetupFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MappingPresetSetup', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['instructions', 'urlTemplate']);
      final val = MappingPresetSetup(
        instructions: $checkedConvert('instructions', (v) => v as String),
        urlTemplate: $checkedConvert('urlTemplate', (v) => v as String),
        url: $checkedConvert('url', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$MappingPresetSetupToJson(MappingPresetSetup instance) =>
    <String, dynamic>{
      'instructions': instance.instructions,
      'urlTemplate': instance.urlTemplate,
      'url': ?instance.url,
    };
