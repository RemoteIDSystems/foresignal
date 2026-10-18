// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_suggest_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingSuggestRequestCWProxy {
  MappingSuggestRequest sample(MappingSample sample);

  MappingSuggestRequest hints(MappingSuggestRequestHints? hints);

  MappingSuggestRequest confirmEgress(bool? confirmEgress);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingSuggestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingSuggestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingSuggestRequest call({
    MappingSample sample,
    MappingSuggestRequestHints? hints,
    bool? confirmEgress,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingSuggestRequest.copyWith(...)` or call `instanceOfMappingSuggestRequest.copyWith.fieldName(value)` for a single field.
class _$MappingSuggestRequestCWProxyImpl
    implements _$MappingSuggestRequestCWProxy {
  const _$MappingSuggestRequestCWProxyImpl(this._value);

  final MappingSuggestRequest _value;

  @override
  MappingSuggestRequest sample(MappingSample sample) => call(sample: sample);

  @override
  MappingSuggestRequest hints(MappingSuggestRequestHints? hints) =>
      call(hints: hints);

  @override
  MappingSuggestRequest confirmEgress(bool? confirmEgress) =>
      call(confirmEgress: confirmEgress);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingSuggestRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingSuggestRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingSuggestRequest call({
    Object? sample = const $CopyWithPlaceholder(),
    Object? hints = const $CopyWithPlaceholder(),
    Object? confirmEgress = const $CopyWithPlaceholder(),
  }) {
    return MappingSuggestRequest(
      sample: sample == const $CopyWithPlaceholder() || sample == null
          ? _value.sample
          // ignore: cast_nullable_to_non_nullable
          : sample as MappingSample,
      hints: hints == const $CopyWithPlaceholder()
          ? _value.hints
          // ignore: cast_nullable_to_non_nullable
          : hints as MappingSuggestRequestHints?,
      confirmEgress: confirmEgress == const $CopyWithPlaceholder()
          ? _value.confirmEgress
          // ignore: cast_nullable_to_non_nullable
          : confirmEgress as bool?,
    );
  }
}

extension $MappingSuggestRequestCopyWith on MappingSuggestRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingSuggestRequest.copyWith(...)` or `instanceOfMappingSuggestRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingSuggestRequestCWProxy get copyWith =>
      _$MappingSuggestRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingSuggestRequest _$MappingSuggestRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingSuggestRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['sample']);
  final val = MappingSuggestRequest(
    sample: $checkedConvert(
      'sample',
      (v) => MappingSample.fromJson(v as Map<String, dynamic>),
    ),
    hints: $checkedConvert(
      'hints',
      (v) => v == null
          ? null
          : MappingSuggestRequestHints.fromJson(v as Map<String, dynamic>),
    ),
    confirmEgress: $checkedConvert('confirmEgress', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$MappingSuggestRequestToJson(
  MappingSuggestRequest instance,
) => <String, dynamic>{
  'sample': instance.sample.toJson(),
  'hints': ?instance.hints?.toJson(),
  'confirmEgress': ?instance.confirmEgress,
};
