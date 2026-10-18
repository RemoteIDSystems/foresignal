// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_active.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestActiveCWProxy {
  DigestActive sourceId(String sourceId);

  DigestActive kind(String kind);

  DigestActive entities(int entities);

  DigestActive previous(int? previous);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestActive(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestActive(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestActive call({
    String sourceId,
    String kind,
    int entities,
    int? previous,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestActive.copyWith(...)` or call `instanceOfDigestActive.copyWith.fieldName(value)` for a single field.
class _$DigestActiveCWProxyImpl implements _$DigestActiveCWProxy {
  const _$DigestActiveCWProxyImpl(this._value);

  final DigestActive _value;

  @override
  DigestActive sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  DigestActive kind(String kind) => call(kind: kind);

  @override
  DigestActive entities(int entities) => call(entities: entities);

  @override
  DigestActive previous(int? previous) => call(previous: previous);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestActive(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestActive(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestActive call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? entities = const $CopyWithPlaceholder(),
    Object? previous = const $CopyWithPlaceholder(),
  }) {
    return DigestActive(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      entities: entities == const $CopyWithPlaceholder() || entities == null
          ? _value.entities
          // ignore: cast_nullable_to_non_nullable
          : entities as int,
      previous: previous == const $CopyWithPlaceholder()
          ? _value.previous
          // ignore: cast_nullable_to_non_nullable
          : previous as int?,
    );
  }
}

extension $DigestActiveCopyWith on DigestActive {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestActive.copyWith(...)` or `instanceOfDigestActive.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestActiveCWProxy get copyWith => _$DigestActiveCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestActive _$DigestActiveFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DigestActive', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['sourceId', 'kind', 'entities']);
      final val = DigestActive(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        kind: $checkedConvert('kind', (v) => v as String),
        entities: $checkedConvert('entities', (v) => (v as num).toInt()),
        previous: $checkedConvert('previous', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$DigestActiveToJson(DigestActive instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'kind': instance.kind,
      'entities': instance.entities,
      'previous': ?instance.previous,
    };
