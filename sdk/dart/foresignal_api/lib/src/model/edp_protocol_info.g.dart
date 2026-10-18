// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_protocol_info.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpProtocolInfoCWProxy {
  EdpProtocolInfo protocol(String protocol);

  EdpProtocolInfo supported(List<String>? supported);

  EdpProtocolInfo itemTypes(List<String> itemTypes);

  EdpProtocolInfo modes(List<EdpProtocolInfoModesEnum> modes);

  EdpProtocolInfo schemas(List<String>? schemas);

  EdpProtocolInfo documentation(String? documentation);

  EdpProtocolInfo maxMinor(int? maxMinor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpProtocolInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpProtocolInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpProtocolInfo call({
    String protocol,
    List<String>? supported,
    List<String> itemTypes,
    List<EdpProtocolInfoModesEnum> modes,
    List<String>? schemas,
    String? documentation,
    int? maxMinor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpProtocolInfo.copyWith(...)` or call `instanceOfEdpProtocolInfo.copyWith.fieldName(value)` for a single field.
class _$EdpProtocolInfoCWProxyImpl implements _$EdpProtocolInfoCWProxy {
  const _$EdpProtocolInfoCWProxyImpl(this._value);

  final EdpProtocolInfo _value;

  @override
  EdpProtocolInfo protocol(String protocol) => call(protocol: protocol);

  @override
  EdpProtocolInfo supported(List<String>? supported) =>
      call(supported: supported);

  @override
  EdpProtocolInfo itemTypes(List<String> itemTypes) =>
      call(itemTypes: itemTypes);

  @override
  EdpProtocolInfo modes(List<EdpProtocolInfoModesEnum> modes) =>
      call(modes: modes);

  @override
  EdpProtocolInfo schemas(List<String>? schemas) => call(schemas: schemas);

  @override
  EdpProtocolInfo documentation(String? documentation) =>
      call(documentation: documentation);

  @override
  EdpProtocolInfo maxMinor(int? maxMinor) => call(maxMinor: maxMinor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpProtocolInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpProtocolInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpProtocolInfo call({
    Object? protocol = const $CopyWithPlaceholder(),
    Object? supported = const $CopyWithPlaceholder(),
    Object? itemTypes = const $CopyWithPlaceholder(),
    Object? modes = const $CopyWithPlaceholder(),
    Object? schemas = const $CopyWithPlaceholder(),
    Object? documentation = const $CopyWithPlaceholder(),
    Object? maxMinor = const $CopyWithPlaceholder(),
  }) {
    return EdpProtocolInfo(
      protocol: protocol == const $CopyWithPlaceholder() || protocol == null
          ? _value.protocol
          // ignore: cast_nullable_to_non_nullable
          : protocol as String,
      supported: supported == const $CopyWithPlaceholder()
          ? _value.supported
          // ignore: cast_nullable_to_non_nullable
          : supported as List<String>?,
      itemTypes: itemTypes == const $CopyWithPlaceholder() || itemTypes == null
          ? _value.itemTypes
          // ignore: cast_nullable_to_non_nullable
          : itemTypes as List<String>,
      modes: modes == const $CopyWithPlaceholder() || modes == null
          ? _value.modes
          // ignore: cast_nullable_to_non_nullable
          : modes as List<EdpProtocolInfoModesEnum>,
      schemas: schemas == const $CopyWithPlaceholder()
          ? _value.schemas
          // ignore: cast_nullable_to_non_nullable
          : schemas as List<String>?,
      documentation: documentation == const $CopyWithPlaceholder()
          ? _value.documentation
          // ignore: cast_nullable_to_non_nullable
          : documentation as String?,
      maxMinor: maxMinor == const $CopyWithPlaceholder()
          ? _value.maxMinor
          // ignore: cast_nullable_to_non_nullable
          : maxMinor as int?,
    );
  }
}

extension $EdpProtocolInfoCopyWith on EdpProtocolInfo {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpProtocolInfo.copyWith(...)` or `instanceOfEdpProtocolInfo.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpProtocolInfoCWProxy get copyWith => _$EdpProtocolInfoCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpProtocolInfo _$EdpProtocolInfoFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpProtocolInfo', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['protocol', 'itemTypes', 'modes']);
      final val = EdpProtocolInfo(
        protocol: $checkedConvert('protocol', (v) => v as String),
        supported: $checkedConvert(
          'supported',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        itemTypes: $checkedConvert(
          'itemTypes',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        modes: $checkedConvert(
          'modes',
          (v) => (v as List<dynamic>)
              .map((e) => $enumDecode(_$EdpProtocolInfoModesEnumEnumMap, e))
              .toList(),
        ),
        schemas: $checkedConvert(
          'schemas',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        documentation: $checkedConvert('documentation', (v) => v as String?),
        maxMinor: $checkedConvert('maxMinor', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$EdpProtocolInfoToJson(EdpProtocolInfo instance) =>
    <String, dynamic>{
      'protocol': instance.protocol,
      'supported': ?instance.supported,
      'itemTypes': instance.itemTypes,
      'modes': instance.modes
          .map((e) => _$EdpProtocolInfoModesEnumEnumMap[e]!)
          .toList(),
      'schemas': ?instance.schemas,
      'documentation': ?instance.documentation,
      'maxMinor': ?instance.maxMinor,
    };

const _$EdpProtocolInfoModesEnumEnumMap = {
  EdpProtocolInfoModesEnum.layer: 'layer',
  EdpProtocolInfoModesEnum.atomic: 'atomic',
};
