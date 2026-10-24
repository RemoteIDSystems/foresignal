// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ownership_chain.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OwnershipChainCWProxy {
  OwnershipChain refs(List<String> refs);

  OwnershipChain direction(OwnershipChainDirectionEnum direction);

  OwnershipChain asOf(DateTime? asOf);

  OwnershipChain generatedAt(DateTime? generatedAt);

  OwnershipChain chains(List<OwnershipChainNode> chains);

  OwnershipChain shared(List<OwnershipChainNode> shared);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OwnershipChain(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OwnershipChain(...).copyWith(id: 12, name: "My name")
  /// ```
  OwnershipChain call({
    List<String> refs,
    OwnershipChainDirectionEnum direction,
    DateTime? asOf,
    DateTime? generatedAt,
    List<OwnershipChainNode> chains,
    List<OwnershipChainNode> shared,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOwnershipChain.copyWith(...)` or call `instanceOfOwnershipChain.copyWith.fieldName(value)` for a single field.
class _$OwnershipChainCWProxyImpl implements _$OwnershipChainCWProxy {
  const _$OwnershipChainCWProxyImpl(this._value);

  final OwnershipChain _value;

  @override
  OwnershipChain refs(List<String> refs) => call(refs: refs);

  @override
  OwnershipChain direction(OwnershipChainDirectionEnum direction) =>
      call(direction: direction);

  @override
  OwnershipChain asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  OwnershipChain generatedAt(DateTime? generatedAt) =>
      call(generatedAt: generatedAt);

  @override
  OwnershipChain chains(List<OwnershipChainNode> chains) =>
      call(chains: chains);

  @override
  OwnershipChain shared(List<OwnershipChainNode> shared) =>
      call(shared: shared);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OwnershipChain(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OwnershipChain(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OwnershipChain call({
    Object? refs = const $CopyWithPlaceholder(),
    Object? direction = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? chains = const $CopyWithPlaceholder(),
    Object? shared = const $CopyWithPlaceholder(),
  }) {
    return OwnershipChain(
      refs: refs == const $CopyWithPlaceholder() || refs == null
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>,
      direction: direction == const $CopyWithPlaceholder() || direction == null
          ? _value.direction
          // ignore: cast_nullable_to_non_nullable
          : direction as OwnershipChainDirectionEnum,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      generatedAt: generatedAt == const $CopyWithPlaceholder()
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime?,
      chains: chains == const $CopyWithPlaceholder() || chains == null
          ? _value.chains
          // ignore: cast_nullable_to_non_nullable
          : chains as List<OwnershipChainNode>,
      shared: shared == const $CopyWithPlaceholder() || shared == null
          ? _value.shared
          // ignore: cast_nullable_to_non_nullable
          : shared as List<OwnershipChainNode>,
    );
  }
}

extension $OwnershipChainCopyWith on OwnershipChain {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOwnershipChain.copyWith(...)` or `instanceOfOwnershipChain.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OwnershipChainCWProxy get copyWith => _$OwnershipChainCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OwnershipChain _$OwnershipChainFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OwnershipChain', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['refs', 'direction', 'chains', 'shared'],
  );
  final val = OwnershipChain(
    refs: $checkedConvert(
      'refs',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    direction: $checkedConvert(
      'direction',
      (v) => $enumDecode(_$OwnershipChainDirectionEnumEnumMap, v),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    generatedAt: $checkedConvert(
      'generatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    chains: $checkedConvert(
      'chains',
      (v) => (v as List<dynamic>)
          .map((e) => OwnershipChainNode.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    shared: $checkedConvert(
      'shared',
      (v) => (v as List<dynamic>)
          .map((e) => OwnershipChainNode.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$OwnershipChainToJson(OwnershipChain instance) =>
    <String, dynamic>{
      'refs': instance.refs,
      'direction': _$OwnershipChainDirectionEnumEnumMap[instance.direction]!,
      'asOf': ?instance.asOf?.toIso8601String(),
      'generatedAt': ?instance.generatedAt?.toIso8601String(),
      'chains': instance.chains.map((e) => e.toJson()).toList(),
      'shared': instance.shared.map((e) => e.toJson()).toList(),
    };

const _$OwnershipChainDirectionEnumEnumMap = {
  OwnershipChainDirectionEnum.in_: 'in',
  OwnershipChainDirectionEnum.out_: 'out',
  OwnershipChainDirectionEnum.both: 'both',
};
