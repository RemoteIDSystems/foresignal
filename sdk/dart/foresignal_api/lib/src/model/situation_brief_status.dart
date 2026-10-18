//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_brief_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationBriefStatus {
  /// Returns a new [SituationBriefStatus] instance.
  SituationBriefStatus({

    required  this.situationId,

    required  this.status,

    required  this.requestedAt,

     this.previousBriefGeneratedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: true,
    includeIfNull: false,
  )


  final String situationId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SituationBriefStatusStatusEnum status;



  @JsonKey(
    
    name: r'requestedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime requestedAt;



  @JsonKey(
    
    name: r'previousBriefGeneratedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? previousBriefGeneratedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationBriefStatus &&
      other.situationId == situationId &&
      other.status == status &&
      other.requestedAt == requestedAt &&
      other.previousBriefGeneratedAt == previousBriefGeneratedAt;

    @override
    int get hashCode =>
        situationId.hashCode +
        status.hashCode +
        requestedAt.hashCode +
        (previousBriefGeneratedAt == null ? 0 : previousBriefGeneratedAt.hashCode);

  factory SituationBriefStatus.fromJson(Map<String, dynamic> json) => _$SituationBriefStatusFromJson(json);

  Map<String, dynamic> toJson() => _$SituationBriefStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SituationBriefStatusStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running');

const SituationBriefStatusStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


