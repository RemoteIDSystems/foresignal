//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'archive_run_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ArchiveRunRequest {
  /// Returns a new [ArchiveRunRequest] instance.
  ArchiveRunRequest({

     this.cutoff,

     this.sources,

     this.dryRun = false,
  });

      /// Override the configured cutoff; archive STANDARD data older than this (must be in the past)
  @JsonKey(
    
    name: r'cutoff',
    required: false,
    includeIfNull: false,
  )


  final DateTime? cutoff;



      /// Restrict to these sources, each of which the archive must cover (400 otherwise); empty uses the configured allowlist
  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



      /// Count what would be archived without exporting or purging; the answer is an Archive with status `pending` and `itemCount`, not stored
  @JsonKey(
    defaultValue: false,
    name: r'dryRun',
    required: false,
    includeIfNull: false,
  )


  final bool? dryRun;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ArchiveRunRequest &&
      other.cutoff == cutoff &&
      other.sources == sources &&
      other.dryRun == dryRun;

    @override
    int get hashCode =>
        (cutoff == null ? 0 : cutoff.hashCode) +
        sources.hashCode +
        dryRun.hashCode;

  factory ArchiveRunRequest.fromJson(Map<String, dynamic> json) => _$ArchiveRunRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ArchiveRunRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

