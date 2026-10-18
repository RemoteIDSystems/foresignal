// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_count_entities.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestCountEntitiesCWProxy {
  DigestCountEntities new_(int? new_);

  DigestCountEntities changed(int? changed);

  DigestCountEntities removed(int? removed);

  DigestCountEntities edges(int? edges);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestCountEntities(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestCountEntities(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestCountEntities call({int? new_, int? changed, int? removed, int? edges});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestCountEntities.copyWith(...)` or call `instanceOfDigestCountEntities.copyWith.fieldName(value)` for a single field.
class _$DigestCountEntitiesCWProxyImpl implements _$DigestCountEntitiesCWProxy {
  const _$DigestCountEntitiesCWProxyImpl(this._value);

  final DigestCountEntities _value;

  @override
  DigestCountEntities new_(int? new_) => call(new_: new_);

  @override
  DigestCountEntities changed(int? changed) => call(changed: changed);

  @override
  DigestCountEntities removed(int? removed) => call(removed: removed);

  @override
  DigestCountEntities edges(int? edges) => call(edges: edges);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestCountEntities(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestCountEntities(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestCountEntities call({
    Object? new_ = const $CopyWithPlaceholder(),
    Object? changed = const $CopyWithPlaceholder(),
    Object? removed = const $CopyWithPlaceholder(),
    Object? edges = const $CopyWithPlaceholder(),
  }) {
    return DigestCountEntities(
      new_: new_ == const $CopyWithPlaceholder()
          ? _value.new_
          // ignore: cast_nullable_to_non_nullable
          : new_ as int?,
      changed: changed == const $CopyWithPlaceholder()
          ? _value.changed
          // ignore: cast_nullable_to_non_nullable
          : changed as int?,
      removed: removed == const $CopyWithPlaceholder()
          ? _value.removed
          // ignore: cast_nullable_to_non_nullable
          : removed as int?,
      edges: edges == const $CopyWithPlaceholder()
          ? _value.edges
          // ignore: cast_nullable_to_non_nullable
          : edges as int?,
    );
  }
}

extension $DigestCountEntitiesCopyWith on DigestCountEntities {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestCountEntities.copyWith(...)` or `instanceOfDigestCountEntities.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestCountEntitiesCWProxy get copyWith =>
      _$DigestCountEntitiesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestCountEntities _$DigestCountEntitiesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DigestCountEntities', json, ($checkedConvert) {
      final val = DigestCountEntities(
        new_: $checkedConvert('new', (v) => (v as num?)?.toInt()),
        changed: $checkedConvert('changed', (v) => (v as num?)?.toInt()),
        removed: $checkedConvert('removed', (v) => (v as num?)?.toInt()),
        edges: $checkedConvert('edges', (v) => (v as num?)?.toInt()),
      );
      return val;
    }, fieldKeyMap: const {'new_': 'new'});

Map<String, dynamic> _$DigestCountEntitiesToJson(
  DigestCountEntities instance,
) => <String, dynamic>{
  'new': ?instance.new_,
  'changed': ?instance.changed,
  'removed': ?instance.removed,
  'edges': ?instance.edges,
};
