// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_count.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestCountCWProxy {
  DigestCount sourceId(String sourceId);

  DigestCount kind(String kind);

  DigestCount new_(int new_);

  DigestCount changed(int changed);

  DigestCount removed(int removed);

  DigestCount edges(int? edges);

  DigestCount routine(int? routine);

  DigestCount entities(DigestCountEntities? entities);

  DigestCount byReason(Map<String, int>? byReason);

  DigestCount previous(DigestCountPrevious? previous);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestCount(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestCount(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestCount call({
    String sourceId,
    String kind,
    int new_,
    int changed,
    int removed,
    int? edges,
    int? routine,
    DigestCountEntities? entities,
    Map<String, int>? byReason,
    DigestCountPrevious? previous,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestCount.copyWith(...)` or call `instanceOfDigestCount.copyWith.fieldName(value)` for a single field.
class _$DigestCountCWProxyImpl implements _$DigestCountCWProxy {
  const _$DigestCountCWProxyImpl(this._value);

  final DigestCount _value;

  @override
  DigestCount sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  DigestCount kind(String kind) => call(kind: kind);

  @override
  DigestCount new_(int new_) => call(new_: new_);

  @override
  DigestCount changed(int changed) => call(changed: changed);

  @override
  DigestCount removed(int removed) => call(removed: removed);

  @override
  DigestCount edges(int? edges) => call(edges: edges);

  @override
  DigestCount routine(int? routine) => call(routine: routine);

  @override
  DigestCount entities(DigestCountEntities? entities) =>
      call(entities: entities);

  @override
  DigestCount byReason(Map<String, int>? byReason) => call(byReason: byReason);

  @override
  DigestCount previous(DigestCountPrevious? previous) =>
      call(previous: previous);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestCount(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestCount(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestCount call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? new_ = const $CopyWithPlaceholder(),
    Object? changed = const $CopyWithPlaceholder(),
    Object? removed = const $CopyWithPlaceholder(),
    Object? edges = const $CopyWithPlaceholder(),
    Object? routine = const $CopyWithPlaceholder(),
    Object? entities = const $CopyWithPlaceholder(),
    Object? byReason = const $CopyWithPlaceholder(),
    Object? previous = const $CopyWithPlaceholder(),
  }) {
    return DigestCount(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      new_: new_ == const $CopyWithPlaceholder() || new_ == null
          ? _value.new_
          // ignore: cast_nullable_to_non_nullable
          : new_ as int,
      changed: changed == const $CopyWithPlaceholder() || changed == null
          ? _value.changed
          // ignore: cast_nullable_to_non_nullable
          : changed as int,
      removed: removed == const $CopyWithPlaceholder() || removed == null
          ? _value.removed
          // ignore: cast_nullable_to_non_nullable
          : removed as int,
      edges: edges == const $CopyWithPlaceholder()
          ? _value.edges
          // ignore: cast_nullable_to_non_nullable
          : edges as int?,
      routine: routine == const $CopyWithPlaceholder()
          ? _value.routine
          // ignore: cast_nullable_to_non_nullable
          : routine as int?,
      entities: entities == const $CopyWithPlaceholder()
          ? _value.entities
          // ignore: cast_nullable_to_non_nullable
          : entities as DigestCountEntities?,
      byReason: byReason == const $CopyWithPlaceholder()
          ? _value.byReason
          // ignore: cast_nullable_to_non_nullable
          : byReason as Map<String, int>?,
      previous: previous == const $CopyWithPlaceholder()
          ? _value.previous
          // ignore: cast_nullable_to_non_nullable
          : previous as DigestCountPrevious?,
    );
  }
}

extension $DigestCountCopyWith on DigestCount {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestCount.copyWith(...)` or `instanceOfDigestCount.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestCountCWProxy get copyWith => _$DigestCountCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestCount _$DigestCountFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DigestCount', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['sourceId', 'kind', 'new', 'changed', 'removed'],
      );
      final val = DigestCount(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        kind: $checkedConvert('kind', (v) => v as String),
        new_: $checkedConvert('new', (v) => (v as num).toInt()),
        changed: $checkedConvert('changed', (v) => (v as num).toInt()),
        removed: $checkedConvert('removed', (v) => (v as num).toInt()),
        edges: $checkedConvert('edges', (v) => (v as num?)?.toInt()),
        routine: $checkedConvert('routine', (v) => (v as num?)?.toInt()),
        entities: $checkedConvert(
          'entities',
          (v) => v == null
              ? null
              : DigestCountEntities.fromJson(v as Map<String, dynamic>),
        ),
        byReason: $checkedConvert(
          'byReason',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
        previous: $checkedConvert(
          'previous',
          (v) => v == null
              ? null
              : DigestCountPrevious.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    }, fieldKeyMap: const {'new_': 'new'});

Map<String, dynamic> _$DigestCountToJson(DigestCount instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'kind': instance.kind,
      'new': instance.new_,
      'changed': instance.changed,
      'removed': instance.removed,
      'edges': ?instance.edges,
      'routine': ?instance.routine,
      'entities': ?instance.entities?.toJson(),
      'byReason': ?instance.byReason,
      'previous': ?instance.previous?.toJson(),
    };
