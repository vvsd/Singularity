Controller = require './Controller'

TaskPending = require '../models/TaskPending'

Tasks = require '../collections/Tasks'
TasksPending = require '../collections/TasksPending'
TaskCleanups = require '../collections/TaskCleanups'
TaskKills = require '../collections/TaskKills'

TasksTableView = require '../views/tasks'

class TasksTableController extends Controller

    initialize: ({@state, @searchFilter}) ->
        @title 'Tasks'

        if @state is 'decommissioning'
            @collections.tasks = new Tasks [], state: 'active'
        else if @state is 'scheduled'
            @collections.tasks = new Tasks [], {state: 'scheduled', addPropertyString: 'pendingTask'}
            # @collections.tasks.setPropertyString('pendingTask')

        else
            @collections.tasks = new Tasks [], {@state}
        @collections.taskCleanups = new TaskCleanups
        @collections.taskKills = new TaskKills

        @setView new TasksTableView _.extend {@state, @searchFilter},
            collection: @collections.tasks
            pendingTasks: @collections.tasksPending
            attributes:
                cleaning: @collections.taskCleanups
                killing: @collections.taskKills

        # Fetch a pending task's full details
        @view.on 'getPendingTask', (task) => @getPendingTask(task)

        @refresh()
        app.showView @view

    getPendingTask: (task) ->
        app.showFixedPageLoader()
        @collections.tasksPending = new TasksPending [], {requestID: task.requestId}
        @collections.tasksPending.fetch().done =>
            utils.viewJSON @collections.tasksPending.getTaskByRuntime(task.nextRunAt), (resp) =>
                if resp.error
                    Messenger().error
                        message:   "<p>This task is no longer pending.</p>"
                    @refresh()
            app.hideFixedPageLoader()

    refresh: ->
        # Don't refresh if user is scrolled down, viewing the table (arbitrary value)
        return if $(window).scrollTop() > 200
        # Don't refresh if the table is sorted
        return if @view.isSorted

        @collections.taskCleanups.fetch()
        @collections.taskKills.fetch()
        @collections.tasks.fetch reset: true

module.exports = TasksTableController
