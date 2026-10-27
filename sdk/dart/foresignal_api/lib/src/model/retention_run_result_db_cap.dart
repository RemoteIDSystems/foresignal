//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'retention_run_result_db_cap.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RetentionRunResultDbCap {
  /// Returns a new [RetentionRunResultDbCap] instance.
  RetentionRunResultDbCap({

     this.capBytes,

     this.sizeBeforeBytes,

     this.sizeAfterBytes,

     this.bytesReclaimed,

     this.windowDays,

     this.steps,

     this.action,

     this.deleted,
  });

  @JsonKey(
    
    name: r'capBytes',
    required: false,
    includeIfNull: false,
  )


  final int? capBytes;



      /// Database size after the window pass
  @JsonKey(
    
    name: r'sizeBeforeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? sizeBeforeBytes;



  @JsonKey(
    
    name: r'sizeAfterBytes',
    required: false,
    includeIfNull: false,
  )


  final int? sizeAfterBytes;



      /// How far the size fell during the cap steps. Deleted rows free space Postgres reuses; files shrink only on a partition drop or VACUUM FULL.
  @JsonKey(
    
    name: r'bytesReclaimed',
    required: false,
    includeIfNull: false,
  )


  final int? bytesReclaimed;



      /// History window in force at the end, or null for the configured windows
  @JsonKey(
    
    name: r'windowDays',
    required: false,
    includeIfNull: false,
  )


  final int? windowDays;



  @JsonKey(
    
    name: r'steps',
    required: false,
    includeIfNull: false,
  )


  final int? steps;



  @JsonKey(
    
    name: r'action',
    required: false,
    includeIfNull: false,
  )


  final String? action;



      /// Rows deleted by the cap steps, by table
  @JsonKey(
    
    name: r'deleted',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? deleted;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RetentionRunResultDbCap &&
      other.capBytes == capBytes &&
      other.sizeBeforeBytes == sizeBeforeBytes &&
      other.sizeAfterBytes == sizeAfterBytes &&
      other.bytesReclaimed == bytesReclaimed &&
      other.windowDays == windowDays &&
      other.steps == steps &&
      other.action == action &&
      other.deleted == deleted;

    @override
    int get hashCode =>
        capBytes.hashCode +
        sizeBeforeBytes.hashCode +
        sizeAfterBytes.hashCode +
        bytesReclaimed.hashCode +
        (windowDays == null ? 0 : windowDays.hashCode) +
        steps.hashCode +
        action.hashCode +
        deleted.hashCode;

  factory RetentionRunResultDbCap.fromJson(Map<String, dynamic> json) => _$RetentionRunResultDbCapFromJson(json);

  Map<String, dynamic> toJson() => _$RetentionRunResultDbCapToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

