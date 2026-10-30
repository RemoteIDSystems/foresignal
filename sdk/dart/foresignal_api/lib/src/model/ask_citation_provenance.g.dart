// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_citation_provenance.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskCitationProvenanceCWProxy {
  AskCitationProvenance refs(List<String> refs);

  AskCitationProvenance reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskCitationProvenance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskCitationProvenance(...).copyWith(id: 12, name: "My name")
  /// ```
  AskCitationProvenance call({List<String> refs, String? reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskCitationProvenance.copyWith(...)` or call `instanceOfAskCitationProvenance.copyWith.fieldName(value)` for a single field.
class _$AskCitationProvenanceCWProxyImpl
    implements _$AskCitationProvenanceCWProxy {
  const _$AskCitationProvenanceCWProxyImpl(this._value);

  final AskCitationProvenance _value;

  @override
  AskCitationProvenance refs(List<String> refs) => call(refs: refs);

  @override
  AskCitationProvenance reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskCitationProvenance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskCitationProvenance(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskCitationProvenance call({
    Object? refs = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return AskCitationProvenance(
      refs: refs == const $CopyWithPlaceholder() || refs == null
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $AskCitationProvenanceCopyWith on AskCitationProvenance {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskCitationProvenance.copyWith(...)` or `instanceOfAskCitationProvenance.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskCitationProvenanceCWProxy get copyWith =>
      _$AskCitationProvenanceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskCitationProvenance _$AskCitationProvenanceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AskCitationProvenance', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['refs']);
  final val = AskCitationProvenance(
    refs: $checkedConvert(
      'refs',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AskCitationProvenanceToJson(
  AskCitationProvenance instance,
) => <String, dynamic>{'refs': instance.refs, 'reason': ?instance.reason};
