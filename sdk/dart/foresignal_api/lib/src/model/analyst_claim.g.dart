// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_claim.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystClaimCWProxy {
  AnalystClaim text(String text);

  AnalystClaim basis(AnalystClaimBasisEnum basis);

  AnalystClaim citations(List<String> citations);

  AnalystClaim downgraded(bool? downgraded);

  AnalystClaim warning(String? warning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystClaim(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystClaim(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystClaim call({
    String text,
    AnalystClaimBasisEnum basis,
    List<String> citations,
    bool? downgraded,
    String? warning,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystClaim.copyWith(...)` or call `instanceOfAnalystClaim.copyWith.fieldName(value)` for a single field.
class _$AnalystClaimCWProxyImpl implements _$AnalystClaimCWProxy {
  const _$AnalystClaimCWProxyImpl(this._value);

  final AnalystClaim _value;

  @override
  AnalystClaim text(String text) => call(text: text);

  @override
  AnalystClaim basis(AnalystClaimBasisEnum basis) => call(basis: basis);

  @override
  AnalystClaim citations(List<String> citations) => call(citations: citations);

  @override
  AnalystClaim downgraded(bool? downgraded) => call(downgraded: downgraded);

  @override
  AnalystClaim warning(String? warning) => call(warning: warning);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystClaim(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystClaim(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystClaim call({
    Object? text = const $CopyWithPlaceholder(),
    Object? basis = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? downgraded = const $CopyWithPlaceholder(),
    Object? warning = const $CopyWithPlaceholder(),
  }) {
    return AnalystClaim(
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      basis: basis == const $CopyWithPlaceholder() || basis == null
          ? _value.basis
          // ignore: cast_nullable_to_non_nullable
          : basis as AnalystClaimBasisEnum,
      citations: citations == const $CopyWithPlaceholder() || citations == null
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<String>,
      downgraded: downgraded == const $CopyWithPlaceholder()
          ? _value.downgraded
          // ignore: cast_nullable_to_non_nullable
          : downgraded as bool?,
      warning: warning == const $CopyWithPlaceholder()
          ? _value.warning
          // ignore: cast_nullable_to_non_nullable
          : warning as String?,
    );
  }
}

extension $AnalystClaimCopyWith on AnalystClaim {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystClaim.copyWith(...)` or `instanceOfAnalystClaim.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystClaimCWProxy get copyWith => _$AnalystClaimCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystClaim _$AnalystClaimFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystClaim', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['text', 'basis', 'citations']);
      final val = AnalystClaim(
        text: $checkedConvert('text', (v) => v as String),
        basis: $checkedConvert(
          'basis',
          (v) => $enumDecode(_$AnalystClaimBasisEnumEnumMap, v),
        ),
        citations: $checkedConvert(
          'citations',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        downgraded: $checkedConvert('downgraded', (v) => v as bool?),
        warning: $checkedConvert('warning', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AnalystClaimToJson(AnalystClaim instance) =>
    <String, dynamic>{
      'text': instance.text,
      'basis': _$AnalystClaimBasisEnumEnumMap[instance.basis]!,
      'citations': instance.citations,
      'downgraded': ?instance.downgraded,
      'warning': ?instance.warning,
    };

const _$AnalystClaimBasisEnumEnumMap = {
  AnalystClaimBasisEnum.evidence: 'evidence',
  AnalystClaimBasisEnum.inference: 'inference',
  AnalystClaimBasisEnum.speculation: 'speculation',
};
