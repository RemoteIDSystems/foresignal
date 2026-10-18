// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'relation_provenance.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RelationProvenanceCWProxy {
  RelationProvenance component(String? component);

  RelationProvenance version(String? version);

  RelationProvenance manifestId(String? manifestId);

  RelationProvenance operator_(String? operator_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RelationProvenance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RelationProvenance(...).copyWith(id: 12, name: "My name")
  /// ```
  RelationProvenance call({
    String? component,
    String? version,
    String? manifestId,
    String? operator_,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRelationProvenance.copyWith(...)` or call `instanceOfRelationProvenance.copyWith.fieldName(value)` for a single field.
class _$RelationProvenanceCWProxyImpl implements _$RelationProvenanceCWProxy {
  const _$RelationProvenanceCWProxyImpl(this._value);

  final RelationProvenance _value;

  @override
  RelationProvenance component(String? component) => call(component: component);

  @override
  RelationProvenance version(String? version) => call(version: version);

  @override
  RelationProvenance manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  @override
  RelationProvenance operator_(String? operator_) => call(operator_: operator_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RelationProvenance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RelationProvenance(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RelationProvenance call({
    Object? component = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? operator_ = const $CopyWithPlaceholder(),
  }) {
    return RelationProvenance(
      component: component == const $CopyWithPlaceholder()
          ? _value.component
          // ignore: cast_nullable_to_non_nullable
          : component as String?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      operator_: operator_ == const $CopyWithPlaceholder()
          ? _value.operator_
          // ignore: cast_nullable_to_non_nullable
          : operator_ as String?,
    );
  }
}

extension $RelationProvenanceCopyWith on RelationProvenance {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRelationProvenance.copyWith(...)` or `instanceOfRelationProvenance.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RelationProvenanceCWProxy get copyWith =>
      _$RelationProvenanceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RelationProvenance _$RelationProvenanceFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RelationProvenance', json, ($checkedConvert) {
      final val = RelationProvenance(
        component: $checkedConvert('component', (v) => v as String?),
        version: $checkedConvert('version', (v) => v as String?),
        manifestId: $checkedConvert('manifestId', (v) => v as String?),
        operator_: $checkedConvert('operator', (v) => v as String?),
      );
      return val;
    }, fieldKeyMap: const {'operator_': 'operator'});

Map<String, dynamic> _$RelationProvenanceToJson(RelationProvenance instance) =>
    <String, dynamic>{
      'component': ?instance.component,
      'version': ?instance.version,
      'manifestId': ?instance.manifestId,
      'operator': ?instance.operator_,
    };
