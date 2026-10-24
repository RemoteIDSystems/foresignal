// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ownership_chain_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OwnershipChainRequestCWProxy {
  OwnershipChainRequest refs(List<String> refs);

  OwnershipChainRequest depth(int? depth);

  OwnershipChainRequest direction(
    OwnershipChainRequestDirectionEnum? direction,
  );

  OwnershipChainRequest types(List<OwnershipChainRequestTypesEnum>? types);

  OwnershipChainRequest minConfidence(num? minConfidence);

  OwnershipChainRequest asOf(DateTime? asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OwnershipChainRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OwnershipChainRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  OwnershipChainRequest call({
    List<String> refs,
    int? depth,
    OwnershipChainRequestDirectionEnum? direction,
    List<OwnershipChainRequestTypesEnum>? types,
    num? minConfidence,
    DateTime? asOf,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOwnershipChainRequest.copyWith(...)` or call `instanceOfOwnershipChainRequest.copyWith.fieldName(value)` for a single field.
class _$OwnershipChainRequestCWProxyImpl
    implements _$OwnershipChainRequestCWProxy {
  const _$OwnershipChainRequestCWProxyImpl(this._value);

  final OwnershipChainRequest _value;

  @override
  OwnershipChainRequest refs(List<String> refs) => call(refs: refs);

  @override
  OwnershipChainRequest depth(int? depth) => call(depth: depth);

  @override
  OwnershipChainRequest direction(
    OwnershipChainRequestDirectionEnum? direction,
  ) => call(direction: direction);

  @override
  OwnershipChainRequest types(List<OwnershipChainRequestTypesEnum>? types) =>
      call(types: types);

  @override
  OwnershipChainRequest minConfidence(num? minConfidence) =>
      call(minConfidence: minConfidence);

  @override
  OwnershipChainRequest asOf(DateTime? asOf) => call(asOf: asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OwnershipChainRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OwnershipChainRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OwnershipChainRequest call({
    Object? refs = const $CopyWithPlaceholder(),
    Object? depth = const $CopyWithPlaceholder(),
    Object? direction = const $CopyWithPlaceholder(),
    Object? types = const $CopyWithPlaceholder(),
    Object? minConfidence = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
  }) {
    return OwnershipChainRequest(
      refs: refs == const $CopyWithPlaceholder() || refs == null
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>,
      depth: depth == const $CopyWithPlaceholder()
          ? _value.depth
          // ignore: cast_nullable_to_non_nullable
          : depth as int?,
      direction: direction == const $CopyWithPlaceholder()
          ? _value.direction
          // ignore: cast_nullable_to_non_nullable
          : direction as OwnershipChainRequestDirectionEnum?,
      types: types == const $CopyWithPlaceholder()
          ? _value.types
          // ignore: cast_nullable_to_non_nullable
          : types as List<OwnershipChainRequestTypesEnum>?,
      minConfidence: minConfidence == const $CopyWithPlaceholder()
          ? _value.minConfidence
          // ignore: cast_nullable_to_non_nullable
          : minConfidence as num?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
    );
  }
}

extension $OwnershipChainRequestCopyWith on OwnershipChainRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOwnershipChainRequest.copyWith(...)` or `instanceOfOwnershipChainRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OwnershipChainRequestCWProxy get copyWith =>
      _$OwnershipChainRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OwnershipChainRequest _$OwnershipChainRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OwnershipChainRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['refs']);
  final val = OwnershipChainRequest(
    refs: $checkedConvert(
      'refs',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    depth: $checkedConvert('depth', (v) => (v as num?)?.toInt() ?? 6),
    direction: $checkedConvert(
      'direction',
      (v) =>
          $enumDecodeNullable(_$OwnershipChainRequestDirectionEnumEnumMap, v),
    ),
    types: $checkedConvert(
      'types',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$OwnershipChainRequestTypesEnumEnumMap, e))
          .toList(),
    ),
    minConfidence: $checkedConvert('minConfidence', (v) => v as num? ?? 0),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$OwnershipChainRequestToJson(
  OwnershipChainRequest instance,
) => <String, dynamic>{
  'refs': instance.refs,
  'depth': ?instance.depth,
  'direction': ?_$OwnershipChainRequestDirectionEnumEnumMap[instance.direction],
  'types': ?instance.types
      ?.map((e) => _$OwnershipChainRequestTypesEnumEnumMap[e]!)
      .toList(),
  'minConfidence': ?instance.minConfidence,
  'asOf': ?instance.asOf?.toIso8601String(),
};

const _$OwnershipChainRequestDirectionEnumEnumMap = {
  OwnershipChainRequestDirectionEnum.in_: 'in',
  OwnershipChainRequestDirectionEnum.out_: 'out',
  OwnershipChainRequestDirectionEnum.both: 'both',
};

const _$OwnershipChainRequestTypesEnumEnumMap = {
  OwnershipChainRequestTypesEnum.owns: 'owns',
  OwnershipChainRequestTypesEnum.controls: 'controls',
  OwnershipChainRequestTypesEnum.officerOf: 'officer-of',
  OwnershipChainRequestTypesEnum.beneficialOwner: 'beneficial-owner',
  OwnershipChainRequestTypesEnum.affiliatedWith: 'affiliated-with',
};
