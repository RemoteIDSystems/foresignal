// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_node_edges.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraNodeEdgesCWProxy {
  InfraNodeEdges in_(List<InfraEdge>? in_);

  InfraNodeEdges out_(List<InfraEdge>? out_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraNodeEdges(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraNodeEdges(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraNodeEdges call({List<InfraEdge>? in_, List<InfraEdge>? out_});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraNodeEdges.copyWith(...)` or call `instanceOfInfraNodeEdges.copyWith.fieldName(value)` for a single field.
class _$InfraNodeEdgesCWProxyImpl implements _$InfraNodeEdgesCWProxy {
  const _$InfraNodeEdgesCWProxyImpl(this._value);

  final InfraNodeEdges _value;

  @override
  InfraNodeEdges in_(List<InfraEdge>? in_) => call(in_: in_);

  @override
  InfraNodeEdges out_(List<InfraEdge>? out_) => call(out_: out_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraNodeEdges(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraNodeEdges(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraNodeEdges call({
    Object? in_ = const $CopyWithPlaceholder(),
    Object? out_ = const $CopyWithPlaceholder(),
  }) {
    return InfraNodeEdges(
      in_: in_ == const $CopyWithPlaceholder()
          ? _value.in_
          // ignore: cast_nullable_to_non_nullable
          : in_ as List<InfraEdge>?,
      out_: out_ == const $CopyWithPlaceholder()
          ? _value.out_
          // ignore: cast_nullable_to_non_nullable
          : out_ as List<InfraEdge>?,
    );
  }
}

extension $InfraNodeEdgesCopyWith on InfraNodeEdges {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraNodeEdges.copyWith(...)` or `instanceOfInfraNodeEdges.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraNodeEdgesCWProxy get copyWith => _$InfraNodeEdgesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraNodeEdges _$InfraNodeEdgesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InfraNodeEdges', json, ($checkedConvert) {
      final val = InfraNodeEdges(
        in_: $checkedConvert(
          'in',
          (v) => (v as List<dynamic>?)
              ?.map((e) => InfraEdge.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        out_: $checkedConvert(
          'out',
          (v) => (v as List<dynamic>?)
              ?.map((e) => InfraEdge.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    }, fieldKeyMap: const {'in_': 'in', 'out_': 'out'});

Map<String, dynamic> _$InfraNodeEdgesToJson(InfraNodeEdges instance) =>
    <String, dynamic>{
      'in': ?instance.in_?.map((e) => e.toJson()).toList(),
      'out': ?instance.out_?.map((e) => e.toJson()).toList(),
    };
