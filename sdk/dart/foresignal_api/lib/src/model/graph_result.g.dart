// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'graph_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GraphResultCWProxy {
  GraphResult nodes(List<ObjectEnvelope> nodes);

  GraphResult edges(List<Relation> edges);

  GraphResult missing(List<String>? missing);

  GraphResult truncated(bool? truncated);

  GraphResult asOf(DateTime? asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GraphResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GraphResult(...).copyWith(id: 12, name: "My name")
  /// ```
  GraphResult call({
    List<ObjectEnvelope> nodes,
    List<Relation> edges,
    List<String>? missing,
    bool? truncated,
    DateTime? asOf,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGraphResult.copyWith(...)` or call `instanceOfGraphResult.copyWith.fieldName(value)` for a single field.
class _$GraphResultCWProxyImpl implements _$GraphResultCWProxy {
  const _$GraphResultCWProxyImpl(this._value);

  final GraphResult _value;

  @override
  GraphResult nodes(List<ObjectEnvelope> nodes) => call(nodes: nodes);

  @override
  GraphResult edges(List<Relation> edges) => call(edges: edges);

  @override
  GraphResult missing(List<String>? missing) => call(missing: missing);

  @override
  GraphResult truncated(bool? truncated) => call(truncated: truncated);

  @override
  GraphResult asOf(DateTime? asOf) => call(asOf: asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GraphResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GraphResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GraphResult call({
    Object? nodes = const $CopyWithPlaceholder(),
    Object? edges = const $CopyWithPlaceholder(),
    Object? missing = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
  }) {
    return GraphResult(
      nodes: nodes == const $CopyWithPlaceholder() || nodes == null
          ? _value.nodes
          // ignore: cast_nullable_to_non_nullable
          : nodes as List<ObjectEnvelope>,
      edges: edges == const $CopyWithPlaceholder() || edges == null
          ? _value.edges
          // ignore: cast_nullable_to_non_nullable
          : edges as List<Relation>,
      missing: missing == const $CopyWithPlaceholder()
          ? _value.missing
          // ignore: cast_nullable_to_non_nullable
          : missing as List<String>?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
    );
  }
}

extension $GraphResultCopyWith on GraphResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGraphResult.copyWith(...)` or `instanceOfGraphResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GraphResultCWProxy get copyWith => _$GraphResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GraphResult _$GraphResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GraphResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['nodes', 'edges']);
      final val = GraphResult(
        nodes: $checkedConvert(
          'nodes',
          (v) => (v as List<dynamic>)
              .map((e) => ObjectEnvelope.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        edges: $checkedConvert(
          'edges',
          (v) => (v as List<dynamic>)
              .map((e) => Relation.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        missing: $checkedConvert(
          'missing',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GraphResultToJson(GraphResult instance) =>
    <String, dynamic>{
      'nodes': instance.nodes.map((e) => e.toJson()).toList(),
      'edges': instance.edges.map((e) => e.toJson()).toList(),
      'missing': ?instance.missing,
      'truncated': ?instance.truncated,
      'asOf': ?instance.asOf?.toIso8601String(),
    };
