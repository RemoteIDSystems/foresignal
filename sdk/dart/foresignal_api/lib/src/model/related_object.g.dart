// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'related_object.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RelatedObjectCWProxy {
  RelatedObject relation(Relation relation);

  RelatedObject direction(RelatedObjectDirectionEnum direction);

  RelatedObject object(ObjectEnvelope object);

  RelatedObject distanceKm(num? distanceKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RelatedObject(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RelatedObject(...).copyWith(id: 12, name: "My name")
  /// ```
  RelatedObject call({
    Relation relation,
    RelatedObjectDirectionEnum direction,
    ObjectEnvelope object,
    num? distanceKm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRelatedObject.copyWith(...)` or call `instanceOfRelatedObject.copyWith.fieldName(value)` for a single field.
class _$RelatedObjectCWProxyImpl implements _$RelatedObjectCWProxy {
  const _$RelatedObjectCWProxyImpl(this._value);

  final RelatedObject _value;

  @override
  RelatedObject relation(Relation relation) => call(relation: relation);

  @override
  RelatedObject direction(RelatedObjectDirectionEnum direction) =>
      call(direction: direction);

  @override
  RelatedObject object(ObjectEnvelope object) => call(object: object);

  @override
  RelatedObject distanceKm(num? distanceKm) => call(distanceKm: distanceKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RelatedObject(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RelatedObject(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RelatedObject call({
    Object? relation = const $CopyWithPlaceholder(),
    Object? direction = const $CopyWithPlaceholder(),
    Object? object = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
  }) {
    return RelatedObject(
      relation: relation == const $CopyWithPlaceholder() || relation == null
          ? _value.relation
          // ignore: cast_nullable_to_non_nullable
          : relation as Relation,
      direction: direction == const $CopyWithPlaceholder() || direction == null
          ? _value.direction
          // ignore: cast_nullable_to_non_nullable
          : direction as RelatedObjectDirectionEnum,
      object: object == const $CopyWithPlaceholder() || object == null
          ? _value.object
          // ignore: cast_nullable_to_non_nullable
          : object as ObjectEnvelope,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
    );
  }
}

extension $RelatedObjectCopyWith on RelatedObject {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRelatedObject.copyWith(...)` or `instanceOfRelatedObject.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RelatedObjectCWProxy get copyWith => _$RelatedObjectCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RelatedObject _$RelatedObjectFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RelatedObject', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['relation', 'direction', 'object']);
      final val = RelatedObject(
        relation: $checkedConvert(
          'relation',
          (v) => Relation.fromJson(v as Map<String, dynamic>),
        ),
        direction: $checkedConvert(
          'direction',
          (v) => $enumDecode(_$RelatedObjectDirectionEnumEnumMap, v),
        ),
        object: $checkedConvert(
          'object',
          (v) => ObjectEnvelope.fromJson(v as Map<String, dynamic>),
        ),
        distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$RelatedObjectToJson(RelatedObject instance) =>
    <String, dynamic>{
      'relation': instance.relation.toJson(),
      'direction': _$RelatedObjectDirectionEnumEnumMap[instance.direction]!,
      'object': instance.object.toJson(),
      'distanceKm': ?instance.distanceKm,
    };

const _$RelatedObjectDirectionEnumEnumMap = {
  RelatedObjectDirectionEnum.out_: 'out',
  RelatedObjectDirectionEnum.in_: 'in',
};
