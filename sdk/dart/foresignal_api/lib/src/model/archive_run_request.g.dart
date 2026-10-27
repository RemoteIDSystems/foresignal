// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive_run_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchiveRunRequestCWProxy {
  ArchiveRunRequest cutoff(DateTime? cutoff);

  ArchiveRunRequest sources(List<String>? sources);

  ArchiveRunRequest dryRun(bool? dryRun);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveRunRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveRunRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ArchiveRunRequest call({
    DateTime? cutoff,
    List<String>? sources,
    bool? dryRun,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchiveRunRequest.copyWith(...)` or call `instanceOfArchiveRunRequest.copyWith.fieldName(value)` for a single field.
class _$ArchiveRunRequestCWProxyImpl implements _$ArchiveRunRequestCWProxy {
  const _$ArchiveRunRequestCWProxyImpl(this._value);

  final ArchiveRunRequest _value;

  @override
  ArchiveRunRequest cutoff(DateTime? cutoff) => call(cutoff: cutoff);

  @override
  ArchiveRunRequest sources(List<String>? sources) => call(sources: sources);

  @override
  ArchiveRunRequest dryRun(bool? dryRun) => call(dryRun: dryRun);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveRunRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveRunRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ArchiveRunRequest call({
    Object? cutoff = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? dryRun = const $CopyWithPlaceholder(),
  }) {
    return ArchiveRunRequest(
      cutoff: cutoff == const $CopyWithPlaceholder()
          ? _value.cutoff
          // ignore: cast_nullable_to_non_nullable
          : cutoff as DateTime?,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      dryRun: dryRun == const $CopyWithPlaceholder()
          ? _value.dryRun
          // ignore: cast_nullable_to_non_nullable
          : dryRun as bool?,
    );
  }
}

extension $ArchiveRunRequestCopyWith on ArchiveRunRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchiveRunRequest.copyWith(...)` or `instanceOfArchiveRunRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchiveRunRequestCWProxy get copyWith =>
      _$ArchiveRunRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ArchiveRunRequest _$ArchiveRunRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ArchiveRunRequest', json, ($checkedConvert) {
      final val = ArchiveRunRequest(
        cutoff: $checkedConvert(
          'cutoff',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        dryRun: $checkedConvert('dryRun', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$ArchiveRunRequestToJson(ArchiveRunRequest instance) =>
    <String, dynamic>{
      'cutoff': ?instance.cutoff?.toIso8601String(),
      'sources': ?instance.sources,
      'dryRun': ?instance.dryRun,
    };
