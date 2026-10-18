// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_ingest_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpIngestResultCWProxy {
  EdpIngestResult accepted(int accepted);

  EdpIngestResult rejected(List<EdpItemRejection> rejected);

  EdpIngestResult outcome(IngestOutcome? outcome);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpIngestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpIngestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpIngestResult call({
    int accepted,
    List<EdpItemRejection> rejected,
    IngestOutcome? outcome,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpIngestResult.copyWith(...)` or call `instanceOfEdpIngestResult.copyWith.fieldName(value)` for a single field.
class _$EdpIngestResultCWProxyImpl implements _$EdpIngestResultCWProxy {
  const _$EdpIngestResultCWProxyImpl(this._value);

  final EdpIngestResult _value;

  @override
  EdpIngestResult accepted(int accepted) => call(accepted: accepted);

  @override
  EdpIngestResult rejected(List<EdpItemRejection> rejected) =>
      call(rejected: rejected);

  @override
  EdpIngestResult outcome(IngestOutcome? outcome) => call(outcome: outcome);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpIngestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpIngestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpIngestResult call({
    Object? accepted = const $CopyWithPlaceholder(),
    Object? rejected = const $CopyWithPlaceholder(),
    Object? outcome = const $CopyWithPlaceholder(),
  }) {
    return EdpIngestResult(
      accepted: accepted == const $CopyWithPlaceholder() || accepted == null
          ? _value.accepted
          // ignore: cast_nullable_to_non_nullable
          : accepted as int,
      rejected: rejected == const $CopyWithPlaceholder() || rejected == null
          ? _value.rejected
          // ignore: cast_nullable_to_non_nullable
          : rejected as List<EdpItemRejection>,
      outcome: outcome == const $CopyWithPlaceholder()
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as IngestOutcome?,
    );
  }
}

extension $EdpIngestResultCopyWith on EdpIngestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpIngestResult.copyWith(...)` or `instanceOfEdpIngestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpIngestResultCWProxy get copyWith => _$EdpIngestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpIngestResult _$EdpIngestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpIngestResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['accepted', 'rejected']);
      final val = EdpIngestResult(
        accepted: $checkedConvert('accepted', (v) => (v as num).toInt()),
        rejected: $checkedConvert(
          'rejected',
          (v) => (v as List<dynamic>)
              .map((e) => EdpItemRejection.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        outcome: $checkedConvert(
          'outcome',
          (v) => v == null
              ? null
              : IngestOutcome.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpIngestResultToJson(EdpIngestResult instance) =>
    <String, dynamic>{
      'accepted': instance.accepted,
      'rejected': instance.rejected.map((e) => e.toJson()).toList(),
      'outcome': ?instance.outcome?.toJson(),
    };
