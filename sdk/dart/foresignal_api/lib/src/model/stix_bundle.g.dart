// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stix_bundle.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StixBundleCWProxy {
  StixBundle type(StixBundleTypeEnum type);

  StixBundle id(String id);

  StixBundle objects(List<Map<String, Object>> objects);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StixBundle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StixBundle(...).copyWith(id: 12, name: "My name")
  /// ```
  StixBundle call({
    StixBundleTypeEnum type,
    String id,
    List<Map<String, Object>> objects,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStixBundle.copyWith(...)` or call `instanceOfStixBundle.copyWith.fieldName(value)` for a single field.
class _$StixBundleCWProxyImpl implements _$StixBundleCWProxy {
  const _$StixBundleCWProxyImpl(this._value);

  final StixBundle _value;

  @override
  StixBundle type(StixBundleTypeEnum type) => call(type: type);

  @override
  StixBundle id(String id) => call(id: id);

  @override
  StixBundle objects(List<Map<String, Object>> objects) =>
      call(objects: objects);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StixBundle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StixBundle(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StixBundle call({
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? objects = const $CopyWithPlaceholder(),
  }) {
    return StixBundle(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as StixBundleTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      objects: objects == const $CopyWithPlaceholder() || objects == null
          ? _value.objects
          // ignore: cast_nullable_to_non_nullable
          : objects as List<Map<String, Object>>,
    );
  }
}

extension $StixBundleCopyWith on StixBundle {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStixBundle.copyWith(...)` or `instanceOfStixBundle.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StixBundleCWProxy get copyWith => _$StixBundleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StixBundle _$StixBundleFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StixBundle', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'id', 'objects']);
      final val = StixBundle(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$StixBundleTypeEnumEnumMap, v),
        ),
        id: $checkedConvert('id', (v) => v as String),
        objects: $checkedConvert(
          'objects',
          (v) => (v as List<dynamic>)
              .map(
                (e) => (e as Map<String, dynamic>).map(
                  (k, e) => MapEntry(k, e as Object),
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StixBundleToJson(StixBundle instance) =>
    <String, dynamic>{
      'type': _$StixBundleTypeEnumEnumMap[instance.type]!,
      'id': instance.id,
      'objects': instance.objects,
    };

const _$StixBundleTypeEnumEnumMap = {StixBundleTypeEnum.bundle: 'bundle'};
