// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'graph_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GraphRequestCWProxy {
  GraphRequest refs(List<String> refs);

  GraphRequest depth(int? depth);

  GraphRequest types(List<GraphRequestTypesEnum>? types);

  GraphRequest asOf(DateTime? asOf);

  GraphRequest minConfidence(num? minConfidence);

  GraphRequest limit(int? limit);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GraphRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GraphRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  GraphRequest call({
    List<String> refs,
    int? depth,
    List<GraphRequestTypesEnum>? types,
    DateTime? asOf,
    num? minConfidence,
    int? limit,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGraphRequest.copyWith(...)` or call `instanceOfGraphRequest.copyWith.fieldName(value)` for a single field.
class _$GraphRequestCWProxyImpl implements _$GraphRequestCWProxy {
  const _$GraphRequestCWProxyImpl(this._value);

  final GraphRequest _value;

  @override
  GraphRequest refs(List<String> refs) => call(refs: refs);

  @override
  GraphRequest depth(int? depth) => call(depth: depth);

  @override
  GraphRequest types(List<GraphRequestTypesEnum>? types) => call(types: types);

  @override
  GraphRequest asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  GraphRequest minConfidence(num? minConfidence) =>
      call(minConfidence: minConfidence);

  @override
  GraphRequest limit(int? limit) => call(limit: limit);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GraphRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GraphRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GraphRequest call({
    Object? refs = const $CopyWithPlaceholder(),
    Object? depth = const $CopyWithPlaceholder(),
    Object? types = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? minConfidence = const $CopyWithPlaceholder(),
    Object? limit = const $CopyWithPlaceholder(),
  }) {
    return GraphRequest(
      refs: refs == const $CopyWithPlaceholder() || refs == null
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>,
      depth: depth == const $CopyWithPlaceholder()
          ? _value.depth
          // ignore: cast_nullable_to_non_nullable
          : depth as int?,
      types: types == const $CopyWithPlaceholder()
          ? _value.types
          // ignore: cast_nullable_to_non_nullable
          : types as List<GraphRequestTypesEnum>?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      minConfidence: minConfidence == const $CopyWithPlaceholder()
          ? _value.minConfidence
          // ignore: cast_nullable_to_non_nullable
          : minConfidence as num?,
      limit: limit == const $CopyWithPlaceholder()
          ? _value.limit
          // ignore: cast_nullable_to_non_nullable
          : limit as int?,
    );
  }
}

extension $GraphRequestCopyWith on GraphRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGraphRequest.copyWith(...)` or `instanceOfGraphRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GraphRequestCWProxy get copyWith => _$GraphRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GraphRequest _$GraphRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GraphRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['refs']);
      final val = GraphRequest(
        refs: $checkedConvert(
          'refs',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        depth: $checkedConvert('depth', (v) => (v as num?)?.toInt() ?? 1),
        types: $checkedConvert(
          'types',
          (v) => (v as List<dynamic>?)
              ?.map((e) => $enumDecode(_$GraphRequestTypesEnumEnumMap, e))
              .toList(),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        minConfidence: $checkedConvert('minConfidence', (v) => v as num?),
        limit: $checkedConvert('limit', (v) => (v as num?)?.toInt() ?? 500),
      );
      return val;
    });

Map<String, dynamic> _$GraphRequestToJson(GraphRequest instance) =>
    <String, dynamic>{
      'refs': instance.refs,
      'depth': ?instance.depth,
      'types': ?instance.types
          ?.map((e) => _$GraphRequestTypesEnumEnumMap[e]!)
          .toList(),
      'asOf': ?instance.asOf?.toIso8601String(),
      'minConfidence': ?instance.minConfidence,
      'limit': ?instance.limit,
    };

const _$GraphRequestTypesEnumEnumMap = {
  GraphRequestTypesEnum.memberOf: 'member-of',
  GraphRequestTypesEnum.observedBy: 'observed-by',
  GraphRequestTypesEnum.sameAs: 'same-as',
  GraphRequestTypesEnum.locatedIn: 'located-in',
  GraphRequestTypesEnum.near: 'near',
  GraphRequestTypesEnum.dependsOn: 'depends-on',
  GraphRequestTypesEnum.affects: 'affects',
  GraphRequestTypesEnum.derivedFrom: 'derived-from',
  GraphRequestTypesEnum.evidenceFor: 'evidence-for',
  GraphRequestTypesEnum.evidenceAgainst: 'evidence-against',
  GraphRequestTypesEnum.about: 'about',
  GraphRequestTypesEnum.mentions: 'mentions',
  GraphRequestTypesEnum.precedes: 'precedes',
  GraphRequestTypesEnum.causes: 'causes',
  GraphRequestTypesEnum.relatedTo: 'related-to',
  GraphRequestTypesEnum.owns: 'owns',
  GraphRequestTypesEnum.controls: 'controls',
  GraphRequestTypesEnum.officerOf: 'officer-of',
  GraphRequestTypesEnum.beneficialOwner: 'beneficial-owner',
  GraphRequestTypesEnum.affiliatedWith: 'affiliated-with',
};
