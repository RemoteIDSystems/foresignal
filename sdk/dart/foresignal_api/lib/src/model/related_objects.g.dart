// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'related_objects.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RelatedObjectsCWProxy {
  RelatedObjects ref(String ref);

  RelatedObjects asOf(DateTime? asOf);

  RelatedObjects items(List<RelatedObject> items);

  RelatedObjects truncated(bool? truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RelatedObjects(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RelatedObjects(...).copyWith(id: 12, name: "My name")
  /// ```
  RelatedObjects call({
    String ref,
    DateTime? asOf,
    List<RelatedObject> items,
    bool? truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRelatedObjects.copyWith(...)` or call `instanceOfRelatedObjects.copyWith.fieldName(value)` for a single field.
class _$RelatedObjectsCWProxyImpl implements _$RelatedObjectsCWProxy {
  const _$RelatedObjectsCWProxyImpl(this._value);

  final RelatedObjects _value;

  @override
  RelatedObjects ref(String ref) => call(ref: ref);

  @override
  RelatedObjects asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  RelatedObjects items(List<RelatedObject> items) => call(items: items);

  @override
  RelatedObjects truncated(bool? truncated) => call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RelatedObjects(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RelatedObjects(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RelatedObjects call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return RelatedObjects(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<RelatedObject>,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
    );
  }
}

extension $RelatedObjectsCopyWith on RelatedObjects {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRelatedObjects.copyWith(...)` or `instanceOfRelatedObjects.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RelatedObjectsCWProxy get copyWith => _$RelatedObjectsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RelatedObjects _$RelatedObjectsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RelatedObjects', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ref', 'items']);
      final val = RelatedObjects(
        ref: $checkedConvert('ref', (v) => v as String),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => RelatedObject.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$RelatedObjectsToJson(RelatedObjects instance) =>
    <String, dynamic>{
      'ref': instance.ref,
      'asOf': ?instance.asOf?.toIso8601String(),
      'items': instance.items.map((e) => e.toJson()).toList(),
      'truncated': ?instance.truncated,
    };
