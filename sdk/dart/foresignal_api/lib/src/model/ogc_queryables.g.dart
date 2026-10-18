// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_queryables.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcQueryablesCWProxy {
  OgcQueryables dollarSchema(String? dollarSchema);

  OgcQueryables dollarId(String? dollarId);

  OgcQueryables type(OgcQueryablesTypeEnum type);

  OgcQueryables title(String? title);

  OgcQueryables properties(Map<String, Map<String, Object>> properties);

  OgcQueryables additionalProperties(bool? additionalProperties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcQueryables(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcQueryables(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcQueryables call({
    String? dollarSchema,
    String? dollarId,
    OgcQueryablesTypeEnum type,
    String? title,
    Map<String, Map<String, Object>> properties,
    bool? additionalProperties,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcQueryables.copyWith(...)` or call `instanceOfOgcQueryables.copyWith.fieldName(value)` for a single field.
class _$OgcQueryablesCWProxyImpl implements _$OgcQueryablesCWProxy {
  const _$OgcQueryablesCWProxyImpl(this._value);

  final OgcQueryables _value;

  @override
  OgcQueryables dollarSchema(String? dollarSchema) =>
      call(dollarSchema: dollarSchema);

  @override
  OgcQueryables dollarId(String? dollarId) => call(dollarId: dollarId);

  @override
  OgcQueryables type(OgcQueryablesTypeEnum type) => call(type: type);

  @override
  OgcQueryables title(String? title) => call(title: title);

  @override
  OgcQueryables properties(Map<String, Map<String, Object>> properties) =>
      call(properties: properties);

  @override
  OgcQueryables additionalProperties(bool? additionalProperties) =>
      call(additionalProperties: additionalProperties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcQueryables(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcQueryables(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcQueryables call({
    Object? dollarSchema = const $CopyWithPlaceholder(),
    Object? dollarId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
    Object? additionalProperties = const $CopyWithPlaceholder(),
  }) {
    return OgcQueryables(
      dollarSchema: dollarSchema == const $CopyWithPlaceholder()
          ? _value.dollarSchema
          // ignore: cast_nullable_to_non_nullable
          : dollarSchema as String?,
      dollarId: dollarId == const $CopyWithPlaceholder()
          ? _value.dollarId
          // ignore: cast_nullable_to_non_nullable
          : dollarId as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as OgcQueryablesTypeEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      properties:
          properties == const $CopyWithPlaceholder() || properties == null
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as Map<String, Map<String, Object>>,
      additionalProperties: additionalProperties == const $CopyWithPlaceholder()
          ? _value.additionalProperties
          // ignore: cast_nullable_to_non_nullable
          : additionalProperties as bool?,
    );
  }
}

extension $OgcQueryablesCopyWith on OgcQueryables {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcQueryables.copyWith(...)` or `instanceOfOgcQueryables.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcQueryablesCWProxy get copyWith => _$OgcQueryablesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcQueryables _$OgcQueryablesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcQueryables', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'properties']);
      final val = OgcQueryables(
        dollarSchema: $checkedConvert(r'$schema', (v) => v as String?),
        dollarId: $checkedConvert(r'$id', (v) => v as String?),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$OgcQueryablesTypeEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        properties: $checkedConvert(
          'properties',
          (v) => (v as Map<String, dynamic>).map(
            (k, e) => MapEntry(
              k,
              (e as Map<String, dynamic>).map(
                (k, e) => MapEntry(k, e as Object),
              ),
            ),
          ),
        ),
        additionalProperties: $checkedConvert(
          'additionalProperties',
          (v) => v as bool?,
        ),
      );
      return val;
    }, fieldKeyMap: const {'dollarSchema': r'$schema', 'dollarId': r'$id'});

Map<String, dynamic> _$OgcQueryablesToJson(OgcQueryables instance) =>
    <String, dynamic>{
      r'$schema': ?instance.dollarSchema,
      r'$id': ?instance.dollarId,
      'type': _$OgcQueryablesTypeEnumEnumMap[instance.type]!,
      'title': ?instance.title,
      'properties': instance.properties,
      'additionalProperties': ?instance.additionalProperties,
    };

const _$OgcQueryablesTypeEnumEnumMap = {OgcQueryablesTypeEnum.object: 'object'};
